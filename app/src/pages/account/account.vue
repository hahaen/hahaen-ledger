<script setup lang="ts">
import CenterModal from '../../components/CenterModal.vue'
import TransactionRow from '../../components/TransactionRow.vue'
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { Account, AccountKind, Transaction, TransactionPage, useLedger } from '../../stores/ledger'
import { request } from '../../utils/api'
import { yuan } from '../../utils/money'

const ledger = useLedger()
const account = ref<Account | null>(null)
const records = ref<Transaction[]>([])
const id = ref(0)
const kind = ref<AccountKind>('FUND')
const formName = ref('')
const fundBalance = ref('0.00')
const creditLimit = ref('0.00')
const currentDebt = ref('0.00')
const included = ref(true)
const saving = ref(false)
const loading = ref(false)
const loadError = ref(false)
const editing = ref(false)
const repayOpen = ref(false)
const groupedRecords = computed(() => {
  const groups: Record<string, Transaction[]> = {}
  for (const record of records.value) (groups[record.occurredAt.slice(0, 10)] ||= []).push(record)
  return Object.entries(groups).sort((left, right) => right[0].localeCompare(left[0]))
})
const recordFilter = ref('')
const repayAmount = ref('0.00')
const isCreate = computed(() => !id.value)
const formKind = computed<AccountKind>(() => account.value?.kind || kind.value)
const filters = [{ value: '', label: '全部' }, { value: 'EXPENSE', label: '支出' }, { value: 'INCOME', label: '收入' }, { value: 'TRANSFER', label: '转账' }, { value: 'REPAYMENT', label: '还款' }]
const typeLabel = (type: string) => ({ EXPENSE: '支出', INCOME: '收入', TRANSFER: '转账', REPAYMENT: '还款' }[type] || type)

onLoad((query) => {
  id.value = Number(query?.id || 0)
  kind.value = query?.kind === 'CREDIT' ? 'CREDIT' : 'FUND'
  if (id.value) void load()
})
onShow(() => { if (id.value) void load() })

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const accountData = await request<Account>(`/api/app/accounts/${id.value}`)
    account.value = accountData
    formName.value = accountData.name
    fundBalance.value = ((accountData.kind === 'FUND' ? accountData.balanceCents : 0) / 100).toFixed(2)
    creditLimit.value = ((accountData.kind === 'CREDIT' ? accountData.creditLimitCents : 0) / 100).toFixed(2)
    currentDebt.value = ((accountData.kind === 'CREDIT' ? accountData.balanceCents : 0) / 100).toFixed(2)
    repayAmount.value = currentDebt.value
    included.value = accountData.includedInNetAsset
    await loadRecords()
  } catch { loadError.value = true } finally { loading.value = false }
}

async function loadRecords() {
  const query = recordFilter.value ? `?type=${recordFilter.value}` : ''
  const page = await request<TransactionPage>(`/api/app/accounts/${id.value}/transactions${query}`)
  records.value = page.items
}

function centsFromInput(value: string, label: string) {
  const text = value.trim()
  if (!/^\d+(\.\d{1,2})?$/.test(text)) throw new Error(`${label}格式不正确`)
  const [yuanPart, centPart = ''] = text.split('.')
  const cents = Number(yuanPart) * 100 + Number(centPart.padEnd(2, '0'))
  if (!Number.isSafeInteger(cents) || cents < 0 || cents > 99_999_999_999) throw new Error(`${label}超出范围`)
  return cents
}

async function save() {
  if (saving.value) return
  saving.value = true
  try {
    const common = { name: formName.value.trim(), kind: formKind.value, includedInNetAsset: included.value }
    if (!common.name) throw new Error('请输入账户名称')
    const payload: Record<string, unknown> = formKind.value === 'FUND'
      ? { ...common, balanceCents: centsFromInput(fundBalance.value, '余额'), creditLimitCents: null, currentDebtCents: null }
      : { ...common, balanceCents: null, creditLimitCents: centsFromInput(creditLimit.value, '总额度'), currentDebtCents: centsFromInput(currentDebt.value, '当前欠款') }
    if (isCreate.value) await ledger.createAccount(payload)
    else await request<Account>(`/api/app/accounts/${id.value}`, { method: 'PUT', data: payload })
    uni.showToast({ title: '账户已保存', icon: 'success' })
    if (isCreate.value) setTimeout(() => uni.navigateBack(), 350)
    else { editing.value = false; await load() }
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '保存失败，请重试', icon: 'none' })
  } finally { saving.value = false }
}

function onIncluded(event: Event) {
  const value = (event as Event & { detail?: { value?: unknown } }).detail?.value
  included.value = Boolean(value)
}
async function repay() {
  if (!account.value || formKind.value !== 'CREDIT' || saving.value) return
  const fund = ledger.state.accounts.find(value => value.kind === 'FUND' && value.status === 'ACTIVE')
  if (!fund) { uni.showToast({ title: '请先创建资金账户', icon: 'none' }); return }
  try {
    const amountCents = centsFromInput(repayAmount.value, '还款金额')
    if (!amountCents) throw new Error('还款金额必须大于0')
    if (amountCents > account.value.balanceCents) throw new Error('还款金额不能超过当前欠款')
    uni.showModal({ title: '确认还款', content: `从 ${fund.name} 还款 ${yuan(amountCents)}？`, success: async result => {
      if (!result.confirm) return
      saving.value = true
      try {
        await request<Transaction>(`/api/app/accounts/${account.value?.id}/repayments`, { method: 'POST', data: { fundAccountId: fund.id, amountCents, idempotencyKey: `repay-${account.value?.id}-${Date.now()}` } })
        uni.showToast({ title: '还款成功', icon: 'success' })
        repayOpen.value = false
        await load()
      } catch { /* request 已显示失败原因 */ } finally { saving.value = false }
    } })
  } catch (error) { uni.showToast({ title: error instanceof Error ? error.message : '还款失败，请重试', icon: 'none' }) }
}

function disable() {
  if (!id.value) return
  uni.showModal({ title: '停用账户', content: '历史流水会保留，新账单不能再选择，确定停用吗？', success: async result => {
    if (!result.confirm) return
    try { await ledger.deleteAccount(id.value); uni.navigateBack() } catch { /* request 已显示失败原因 */ }
  } })
}
function openTransaction(transactionId: number) { uni.navigateTo({ url: `/pages/detail/detail?id=${transactionId}` }) }
</script>

<template>
  <view class="page detail-page account-page">
    <view class="screen-nav"><button class="back nav-side" aria-label="返回" @click="uni.navigateBack()">‹</button><text class="page-title">{{ isCreate ? '新增账户' : '账户详情' }}</text><view class="nav-side" /></view>
    <view v-if="loading && !account" class="card empty">正在加载账户…</view>
    <view v-else-if="loadError" class="card empty">暂时无法加载账户<button class="text-button" @click="load">重试</button></view>
    <template v-else>
      <template v-if="!isCreate && account">
        <view :class="['detail-hero', 'fund-hero', { 'credit-hero': formKind === 'CREDIT' }]"><view class="fund-heading"><view><text class="fund-name">{{ account.name }}</text><text class="account-desc">{{ formKind === 'FUND' ? '资金账户' : '信贷账户' }}</text></view><text class="fund-included">{{ account.includedInNetAsset ? '计入净资产' : '不计入净资产' }}</text></view><text class="fund-balance-label">{{ formKind === 'FUND' ? '当前余额' : '当前欠款' }}</text><view class="detail-amount"><text class="currency">¥</text>{{ yuan(account.balanceCents).slice(1) }}</view><view v-if="formKind === 'CREDIT'" class="credit-stats"><view>总额度<text>{{ yuan(account.creditLimitCents) }}</text></view><view>可用额度<text>{{ yuan(account.creditLimitCents - account.balanceCents) }}</text></view></view><view class="detail-orbit orbit-one" /></view>
        <view class="detail-section"><view class="detail-section-heading"><text class="section-title">账户流水</text><text class="section-meta">{{ records.length }} 笔记录</text></view><view class="filter-row"><button v-for="filter in filters" :key="filter.value" :class="{ active: recordFilter === filter.value }" @click="recordFilter = filter.value; loadRecords()">{{ filter.label }}</button></view><view v-if="!records.length" class="fund-empty"><text class="empty-symbol">◷</text><text class="empty-title">暂无相关记录</text><text>这个账户的收支会在这里呈现</text></view><view v-for="group in groupedRecords" :key="group[0]" class="date-group"><view class="date-heading"><text class="date-title">{{ group[0] }}</text><text class="section-meta">{{ group[1].length }} 笔</text></view><view class="transaction-list"><TransactionRow v-for="record in group[1]" :key="record.id" :transaction="record" @open="openTransaction" /></view></view></view>
        <view class="detail-actions"><button class="detail-action" :disabled="saving" @click="editing = true">编辑账户</button><button v-if="formKind === 'CREDIT'" class="detail-action refund" :disabled="saving || !account.balanceCents" @click="repayOpen = true">{{ account.balanceCents ? '还款' : '已还清' }}</button><button class="detail-action delete" :disabled="saving" @click="disable">停用账户</button></view>
      </template>
      <view v-if="isCreate" class="account-create"><text class="account-form-caption">{{ formKind === 'FUND' ? '资金账户' : '信贷账户' }}</text><view class="fields-card account-form"><view class="field"><label>账户名称</label><input v-model="formName" maxlength="20" placeholder="如：微信、银行卡" /></view><view v-if="formKind === 'FUND'" class="field"><label>当前余额（元）</label><input v-model="fundBalance" type="digit" /></view><template v-else><view class="field"><label>总额度（元）</label><input v-model="creditLimit" type="digit" /></view><view class="field"><label>当前欠款（元）</label><input v-model="currentDebt" type="digit" /></view></template><view class="account-row field"><text>计入净资产</text><switch :checked="included" color="#49AD9C" @change="onIncluded" /></view></view><button class="primary-btn" :disabled="saving || !formName.trim()" @click="save">{{ saving ? '保存中…' : '保存账户' }}</button></view>
    </template>
    <CenterModal v-if="editing" title="编辑账户" @close="!saving && (editing = false)"><view class="field"><label>账户名称</label><input v-model="formName" maxlength="20" placeholder="请输入账户名称" :disabled="saving" /></view><view v-if="formKind === 'FUND'" class="field"><label>当前余额（元）</label><input v-model="fundBalance" type="digit" :disabled="saving" /></view><template v-else><view class="field"><label>总额度（元）</label><input v-model="creditLimit" type="digit" :disabled="saving" /></view><view class="field"><label>当前欠款（元）</label><input v-model="currentDebt" type="digit" :disabled="saving" /></view></template><view class="account-row field"><text>计入净资产</text><switch :checked="included" :disabled="saving" color="#49AD9C" @change="onIncluded" /></view><template #actions><view class="sheet-actions"><button class="secondary-btn" :disabled="saving" @click="editing = false">取消</button><button class="primary-btn" :disabled="saving || !formName.trim()" @click="save">{{ saving ? '保存中…' : '保存' }}</button></view></template></CenterModal>
    <CenterModal v-if="repayOpen" title="账户还款" @close="!saving && (repayOpen = false)"><text class="confirm-copy">当前欠款 {{ yuan(account?.balanceCents) }}</text><view class="field"><label>本次还款金额（元）</label><input v-model="repayAmount" type="digit" :disabled="saving" /></view><template #actions><view class="sheet-actions"><button class="secondary-btn" :disabled="saving" @click="repayOpen = false">取消</button><button class="primary-btn" :disabled="saving" @click="repay">{{ saving ? '还款中…' : '确认还款' }}</button></view></template></CenterModal>
  </view>
</template>
