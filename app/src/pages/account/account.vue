<script setup lang="ts">
import TransactionRow from '../../components/TransactionRow.vue'
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import { Account, AccountKind, Transaction, TransactionPage, useLedger } from '../../stores/ledger'
import { request } from '../../utils/api'
import { formatYuan } from '../../utils/money'
import { stringId } from '../../utils/id'

const ledger = useLedger()
const account = ref<Account | null>(null)
const records = ref<Transaction[]>([])
const id = ref('')
const formName = ref('')
const fundBalance = ref('0')
const creditLimit = ref('0')
const currentDebt = ref('0')
const included = ref(true)
const saving = ref(false)
const loading = ref(false)
const loadError = ref(false)
const editing = ref(false)
const repayOpen = ref(false)
const repayAccountOpen = ref(false)
const repayFundId = ref('')
const deleteOpen = ref(false)
const deleting = ref(false)
const groupedRecords = computed(() => {
  const groups: Record<string, Transaction[]> = {}
  for (const record of records.value) (groups[record.occurredAt.slice(0, 10)] ||= []).push(record)
  return Object.entries(groups).sort((left, right) => right[0].localeCompare(left[0]))
})
const recordFilter = ref('')
const repayAmount = ref('0')
const formKind = computed<AccountKind>(() => account.value?.kind || 'FUND')
const fundAccounts = computed(() => ledger.state.accounts.filter(value => value.kind === 'FUND' && value.status === 'ACTIVE'))
const selectedRepayFund = computed(() => fundAccounts.value.find(value => value.id === repayFundId.value))
const filters = [{ value: '', label: '全部' }, { value: 'EXPENSE', label: '支出' }, { value: 'INCOME', label: '收入' }, { value: 'TRANSFER', label: '转账' }, { value: 'REPAYMENT', label: '还款' }]
const typeLabel = (type: string) => ({ EXPENSE: '支出', INCOME: '收入', TRANSFER: '转账', REPAYMENT: '还款' }[type] || type)

onLoad((query) => {
  id.value = stringId(query?.id)
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
    fundBalance.value = formatYuan(accountData.kind === 'FUND' ? accountData.balanceCents : 0)
    creditLimit.value = formatYuan(accountData.kind === 'CREDIT' ? accountData.creditLimitCents : 0)
    currentDebt.value = formatYuan(accountData.kind === 'CREDIT' ? accountData.balanceCents : 0)
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
  if (saving.value || !account.value) return
  saving.value = true
  try {
    const common = { name: formName.value.trim(), kind: formKind.value, includedInNetAsset: included.value }
    if (!common.name) throw new Error('请输入账户名称')
    const payload: Record<string, unknown> = formKind.value === 'FUND'
      ? { ...common, balanceCents: centsFromInput(fundBalance.value, '余额'), creditLimitCents: null, currentDebtCents: null }
      : { ...common, balanceCents: null, creditLimitCents: centsFromInput(creditLimit.value, '总额度'), currentDebtCents: centsFromInput(currentDebt.value, '当前欠款') }
    await request<Account>(`/api/app/accounts/${id.value}`, { method: 'PUT', data: payload })
    uni.showToast({ title: '账户已保存', icon: 'success' })
    editing.value = false
    await load()
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '保存失败，请重试', icon: 'none' })
  } finally { saving.value = false }
}

function onIncluded(event: Event) {
  const value = (event as Event & { detail?: { value?: unknown } }).detail?.value
  included.value = Boolean(value)
}
async function openRepay() {
  if (!account.value || formKind.value !== 'CREDIT' || saving.value || deleting.value) return
  if (!fundAccounts.value.length) {
    try { await ledger.refresh() } catch { /* request 已显示失败原因 */ }
  }
  const firstFund = fundAccounts.value[0]
  if (!firstFund) { uni.showToast({ title: '请先创建资金账户', icon: 'none' }); return }
  repayFundId.value = ''
  repayAmount.value = currentDebt.value
  repayOpen.value = true
}
function openRepayAccountPicker() {
  if (saving.value) return
  repayOpen.value = false
  repayAccountOpen.value = true
}
function selectRepayAccount(fundId: string) {
  repayFundId.value = fundId
  repayAccountOpen.value = false
  repayOpen.value = true
}
function returnToRepay() {
  repayAccountOpen.value = false
  repayOpen.value = true
}
async function repay() {
  if (!account.value || formKind.value !== 'CREDIT' || saving.value) return
  const fund = selectedRepayFund.value
  if (!fund) { uni.showToast({ title: '请选择还款账户', icon: 'none' }); openRepayAccountPicker(); return }
  try {
    if (!repayAmount.value.trim()) throw new Error('请输入还款金额')
    const amountCents = centsFromInput(repayAmount.value, '还款金额')
    if (!amountCents) throw new Error('还款金额必须大于0')
    if (amountCents > account.value.balanceCents) throw new Error('还款金额不能超过当前欠款')
    if (amountCents > fund.balanceCents) throw new Error('还款金额不能超过还款账户余额')
    saving.value = true
    await request<Transaction>(`/api/app/accounts/${account.value.id}/repayments`, { method: 'POST', data: { fundAccountId: fund.id, amountCents, idempotencyKey: `repay-${account.value.id}-${Date.now()}` } })
    uni.showToast({ title: '还款成功', icon: 'success' })
    repayOpen.value = false
    await load()
  } catch (error) { uni.showToast({ title: error instanceof Error ? error.message : '还款失败，请重试', icon: 'none' }) }
  finally { saving.value = false }
}

function remove() {
  if (!id.value || saving.value || deleting.value) return
  deleteOpen.value = true
}
async function confirmRemove() {
  if (!id.value || deleting.value) return
  deleting.value = true
  try {
    await ledger.deleteAccount(id.value)
    deleteOpen.value = false
    uni.navigateBack()
  } catch {
    /* request 已显示失败原因 */
  } finally { deleting.value = false }
}
function openTransaction(transactionId: string) { uni.navigateTo({ url: `/pages/detail/detail?id=${transactionId}` }) }
</script>

<template>
  <view class="page detail-page account-page">
    <view class="screen-nav"><button class="back nav-side" aria-label="返回" @click="uni.navigateBack()">‹</button><text class="page-title">账户详情</text><view class="nav-side" /></view>
    <view v-if="loading && !account" class="card empty">正在加载账户…</view>
    <view v-else-if="loadError" class="card empty">暂时无法加载账户<button class="text-button" @click="load">重试</button></view>
    <template v-else>
      <template v-if="account">
        <view :class="['detail-hero', 'fund-hero', { 'credit-hero': formKind === 'CREDIT' }]"><view class="fund-heading"><view><text class="fund-name">{{ account.name }}</text><text class="account-desc">{{ formKind === 'FUND' ? '资金账户' : '信贷账户' }}</text></view><text class="fund-included">{{ account.includedInNetAsset ? '计入净资产' : '不计入净资产' }}</text></view><text class="fund-balance-label">{{ formKind === 'FUND' ? '当前余额' : '当前欠款' }}</text><MoneyDisplay class="detail-amount" :value="account.balanceCents" /><view v-if="formKind === 'CREDIT'" class="credit-stats"><view>总额度<MoneyDisplay :value="account.creditLimitCents" /></view><view>可用额度<MoneyDisplay :value="account.creditLimitCents - account.balanceCents" /></view></view><view class="detail-orbit orbit-one" /></view>
        <view class="detail-section"><view class="detail-section-heading"><text class="section-title">账户流水</text><text class="section-meta">{{ records.length }} 笔记录</text></view><view class="filter-row"><button v-for="filter in filters" :key="filter.value" :class="{ active: recordFilter === filter.value }" @click="recordFilter = filter.value; loadRecords()">{{ filter.label }}</button></view><view v-if="!records.length" class="fund-empty"><text class="empty-symbol">◷</text><text class="empty-title">暂无相关记录</text><text>这个账户的收支会在这里呈现</text></view><view v-for="group in groupedRecords" :key="group[0]" class="date-group"><view class="date-heading"><text class="date-title">{{ group[0] }}</text><text class="section-meta">{{ group[1].length }} 笔</text></view><view class="transaction-list"><TransactionRow v-for="record in group[1]" :key="record.id" :transaction="record" @open="openTransaction" /></view></view></view>
        <view class="detail-actions"><button class="detail-action" :disabled="saving || deleting" @click="editing = true">编辑账户</button><button v-if="formKind === 'CREDIT'" class="detail-action refund" :disabled="saving || deleting || !account.balanceCents" @click="openRepay">{{ account.balanceCents ? '还款' : '已还清' }}</button><button class="detail-action delete" :disabled="saving || deleting" @click="remove">删除账号</button></view>
      </template>
    </template>
    <view v-if="editing" class="asset-create-backdrop" @click.self="!saving && (editing = false)" @touchmove.stop.prevent>
      <view class="asset-create-modal asset-edit-modal" role="dialog" aria-modal="true" :aria-label="formKind === 'FUND' ? '编辑资产账户' : '编辑信贷账户'">
        <view class="asset-create-handle" />
        <text class="asset-create-title">{{ formKind === 'FUND' ? '编辑资产账户' : '编辑信贷账户' }}</text>
        <view class="asset-create-fields asset-edit-fields">
          <view class="asset-create-row"><text>账户名称</text><input v-model="formName" maxlength="20" placeholder="请输入账户名称" aria-required="true" :disabled="saving" /></view>
          <view v-if="formKind === 'FUND'" class="asset-create-row"><text>余额</text><view class="asset-create-money"><input v-model="fundBalance" type="digit" inputmode="decimal" placeholder="0" aria-required="true" :disabled="saving" /></view></view>
          <template v-else><view class="asset-create-row"><text>总额度</text><view class="asset-create-money"><input v-model="creditLimit" type="digit" inputmode="decimal" placeholder="0" aria-required="true" :disabled="saving" /></view></view><view class="asset-create-row"><text>当前欠款</text><view class="asset-create-money"><input v-model="currentDebt" type="digit" inputmode="decimal" placeholder="0" aria-required="true" :disabled="saving" /></view></view></template>
          <view class="asset-create-row asset-create-switch"><text>计入净资产</text><switch :checked="included" aria-required="true" :disabled="saving" color="#49AD9C" @change="onIncluded" /></view>
        </view>
        <view class="asset-create-actions"><button class="asset-create-cancel" :disabled="saving" @click="editing = false">取消</button><button class="asset-create-save" :disabled="saving" @click="save">{{ saving ? '保存中…' : '保存账户' }}</button></view>
      </view>
    </view>
    <view v-if="deleteOpen" class="asset-create-backdrop" @click.self="!deleting && (deleteOpen = false)" @touchmove.stop.prevent>
      <view class="account-delete-modal" role="dialog" aria-modal="true" aria-label="删除这个账户">
        <view class="asset-create-handle" />
        <text class="account-delete-title">删除这个账户?</text>
        <text class="account-delete-copy">删除“{{ account?.name }}”后，该账户将从资产列表移除，其余额不再计入净资产。历史账单仍会保留。</text>
        <view class="account-delete-actions"><button class="account-delete-cancel" :disabled="deleting" @click="deleteOpen = false">取消</button><button class="account-delete-confirm" :disabled="deleting" @click="confirmRemove">{{ deleting ? '删除中…' : '确认删除' }}</button></view>
      </view>
    </view>
    <view v-if="repayOpen" class="asset-create-backdrop" @click.self="!saving && (repayOpen = false)" @touchmove.stop.prevent>
      <view class="repay-modal" role="dialog" aria-modal="true" aria-label="还款">
        <view class="asset-create-handle" />
        <text class="repay-title">还款 · {{ account?.name }}</text>
        <view class="repay-copy">当前欠款 <MoneyDisplay :value="account?.balanceCents" />，还款后恢复相应可用额度。</view>
        <button class="repay-account-picker" :disabled="saving" @click="openRepayAccountPicker"><image class="repay-account-icon" src="/static/prototype/funds-account.png" mode="aspectFit" /><view class="repay-account-copy"><text class="repay-account-label">还款账户</text><text class="repay-account-name">{{ selectedRepayFund?.name || '请选择还款账户' }}</text></view><view class="repay-account-balance"><template v-if="selectedRepayFund"><text>余额</text><MoneyDisplay :value="selectedRepayFund.balanceCents" /></template><text v-else>请选择账户</text><text class="arrow">›</text></view></button>
        <view class="repay-amount-row"><text>还款金额</text><view class="repay-amount-money"><input v-model="repayAmount" type="digit" inputmode="decimal" placeholder="0" aria-required="true" :disabled="saving" /></view></view>
        <view class="repay-actions"><button class="repay-cancel" :disabled="saving" @click="repayOpen = false">取消</button><button class="repay-confirm" :disabled="saving" @click="repay">{{ saving ? '还款中…' : '确认还款' }}</button></view>
      </view>
    </view>
    <view v-if="repayAccountOpen" class="asset-create-backdrop repay-picker-backdrop" @click.self="!saving && (repayAccountOpen = false)" @touchmove.stop.prevent>
      <view class="repay-account-modal" role="dialog" aria-modal="true" aria-label="选择还款账户">
        <view class="asset-create-handle" />
        <text class="repay-title">选择还款账户</text>
        <view class="repay-choice-list"><button v-for="fund in fundAccounts" :key="fund.id" :class="['repay-choice-item', { selected: repayFundId === fund.id }]" :disabled="saving" @click="selectRepayAccount(fund.id)"><image class="repay-account-icon" src="/static/prototype/funds-account.png" mode="aspectFit" /><view class="repay-account-copy"><text class="repay-account-name">{{ fund.name }}</text><view class="repay-choice-balance"><text>可用余额</text><MoneyDisplay :value="fund.balanceCents" /></view></view><text class="repay-choice-check">{{ repayFundId === fund.id ? '✓' : '' }}</text></button></view>
        <button class="repay-return" :disabled="saving" @click="returnToRepay">返回还款</button>
      </view>
    </view>
  </view>
</template>
