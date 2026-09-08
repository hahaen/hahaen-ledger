<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import BottomNav from '../../components/BottomNav.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import PageHeader from '../../components/PageHeader.vue'
import { request } from '../../utils/api'
import { Account, AccountKind, AssetOverview, useLedger } from '../../stores/ledger'

const ledger = useLedger()
const state = ledger.state
const overview = ref<AssetOverview | null>(null)
const loading = ref(false)
const createOpen = ref(false)
const createKind = ref<AccountKind>('FUND')
const createName = ref('')
const createBalance = ref('')
const createCreditLimit = ref('')
const createCurrentDebt = ref('')
const createIncluded = ref(true)
const savingAccount = ref(false)
const error = ref(false)
const fundsExpanded = ref(true)
const creditsExpanded = ref(true)
const reorderingAccountId = ref<string | null>(null)
const reorderingKind = ref<AccountKind | null>(null)
const reorderingSaving = ref(false)
const accountOrder = (left: Account, right: Account) => left.sortOrder - right.sortOrder || left.id.localeCompare(right.id)
const funds = computed(() => (overview.value?.accounts || state.accounts).filter(account => account.kind === 'FUND' && account.status === 'ACTIVE').sort(accountOrder))
const credits = computed(() => (overview.value?.accounts || state.accounts).filter(account => account.kind === 'CREDIT' && account.status === 'ACTIVE').sort(accountOrder))

async function load() {
  if (!state.token) return
  loading.value = true
  error.value = false
  try {
    const result = await request<AssetOverview>('/api/app/assets/overview')
    overview.value = result
    state.accounts = result.accounts
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}
function open(id: string) { uni.navigateTo({ url: `/pages/account/account?id=${id}` }) }
function retry() { void load() }
function startReorder(account: Account) {
  if (reorderingSaving.value) return
  const sameKindAccounts = account.kind === 'FUND' ? funds.value : credits.value
  if (sameKindAccounts.length < 2) {
    uni.showToast({ title: '至少需要两个同类账户', icon: 'none' })
    return
  }
  reorderingAccountId.value = account.id
  reorderingKind.value = account.kind
  uni.showToast({ title: '请选择要交换的账户', icon: 'none' })
}
function cancelReorder() {
  if (reorderingSaving.value) return
  reorderingAccountId.value = null
  reorderingKind.value = null
}
async function handleAccountTap(account: Account) {
  if (reorderingSaving.value) return
  const sourceId = reorderingAccountId.value
  if (!sourceId) { open(account.id); return }
  if (sourceId === account.id) { cancelReorder(); return }
  const source = (account.kind === 'FUND' ? funds.value : credits.value).find(item => item.id === sourceId)
  if (!source || source.kind !== account.kind) {
    uni.showToast({ title: '只能交换同类账户', icon: 'none' })
    return
  }
  reorderingSaving.value = true
  try {
    await ledger.reorderAccounts(source.id, account.id, source.sortOrder, account.sortOrder)
    await load()
    reorderingAccountId.value = null
    reorderingKind.value = null
    uni.showToast({ title: '顺序已更新', icon: 'success' })
  } catch {
    reorderingAccountId.value = null
    reorderingKind.value = null
    uni.showToast({ title: '换序失败，请重试', icon: 'none' })
  } finally { reorderingSaving.value = false }
}
function handleAccountRowTap(account: Account) {
  if (!reorderingKind.value) open(account.id)
}
function openCreate() {
  createKind.value = 'FUND'
  createName.value = ''
  createBalance.value = ''
  createCreditLimit.value = ''
  createCurrentDebt.value = ''
  createIncluded.value = true
  createOpen.value = true
}
function closeCreate() { if (!savingAccount.value) createOpen.value = false }
function centsFromInput(value: string, label: string) {
  const text = value.trim()
  if (!/^\d+(\.\d{1,2})?$/.test(text)) throw new Error(`${label}格式不正确`)
  const [yuanPart, centPart = ''] = text.split('.')
  const cents = Number(yuanPart) * 100 + Number(centPart.padEnd(2, '0'))
  if (!Number.isSafeInteger(cents) || cents < 0 || cents > 99_999_999_999) throw new Error(`${label}超出范围`)
  return cents
}
function onIncluded(event: Event) {
  const value = (event as Event & { detail?: { value?: unknown } }).detail?.value
  createIncluded.value = Boolean(value)
}
async function saveAccount() {
  if (savingAccount.value) return
  savingAccount.value = true
  try {
    const name = createName.value.trim()
    if (!name) throw new Error('请输入账户名称')
    if (createKind.value === 'FUND' && !createBalance.value.trim()) throw new Error('请输入余额')
    if (createKind.value === 'CREDIT' && !createCreditLimit.value.trim()) throw new Error('请输入总额度')
    if (createKind.value === 'CREDIT' && !createCurrentDebt.value.trim()) throw new Error('请输入当前欠款')
    const common = { name, kind: createKind.value, includedInNetAsset: createIncluded.value }
    const payload: Record<string, unknown> = createKind.value === 'FUND'
      ? { ...common, balanceCents: centsFromInput(createBalance.value, '余额'), creditLimitCents: null, currentDebtCents: null }
      : { ...common, balanceCents: null, creditLimitCents: centsFromInput(createCreditLimit.value, '总额度'), currentDebtCents: centsFromInput(createCurrentDebt.value, '当前欠款') }
    await ledger.createAccount(payload)
    await load()
    createOpen.value = false
    uni.showToast({ title: '账户已保存', icon: 'success' })
  } catch (saveError) {
    uni.showToast({ title: saveError instanceof Error ? saveError.message : '保存失败，请重试', icon: 'none' })
  } finally { savingAccount.value = false }
}

onShow(() => { void load() })
</script>

<template>
  <view class="page assets-page">
    <PageHeader />
    <view class="asset-hero"><text class="asset-label">净资产</text><MoneyDisplay class="asset-net" :value="overview?.netAssetsCents" /><view class="asset-breakdown"><view><text>总资产</text><MoneyDisplay class="breakdown-value" :value="overview?.totalAssetsCents" /></view><view><text>总负债</text><MoneyDisplay class="breakdown-value liability" :value="overview?.totalLiabilitiesCents" /></view></view></view>
    <view v-if="loading" class="card empty">正在加载资产…</view>
    <view v-else-if="error" class="card empty">暂时无法加载资产<button class="text-button" @click="retry">重试</button></view>
    <template v-else>
      <view class="account-section">
        <button class="account-heading" :aria-expanded="creditsExpanded" @click="creditsExpanded = !creditsExpanded"><text>信贷账户</text><view class="account-toggle">{{ creditsExpanded ? '收起' : '展开' }}</view></button>
        <view v-if="creditsExpanded" class="account-list">
          <view v-if="!credits.length" class="list-empty">暂无信贷账户</view>
          <view v-for="account in credits" :key="account.id" role="button" :aria-label="account.name" :class="['account-item', { 'reorder-selected': reorderingAccountId === account.id }]" @click="handleAccountRowTap(account)" @longpress="startReorder(account)"><image class="account-icon credit" src="/static/prototype/credit-account.png" mode="aspectFit" /><view class="account-main"><view class="account-name-row"><text class="account-name">{{ account.name }}</text><text v-if="!account.includedInNetAsset" class="account-exclusion-badge">不计入</text></view><view class="credit-available"><text>可用</text><MoneyDisplay :value="account.creditLimitCents - account.balanceCents" /></view></view><view class="account-balance liability"><text class="balance-caption">欠款</text><MoneyDisplay :value="account.balanceCents" /></view><view v-if="reorderingKind === 'CREDIT'" class="account-reorder-action" @click.stop.prevent="handleAccountTap(account)">交换</view><text v-else class="arrow">›</text></view>
        </view>
      </view>
      <view class="account-section">
        <button class="account-heading" :aria-expanded="fundsExpanded" @click="fundsExpanded = !fundsExpanded"><text>资金账户</text><view class="account-toggle">{{ fundsExpanded ? '收起' : '展开' }}</view></button>
        <view v-if="fundsExpanded" class="account-list">
          <view v-if="!funds.length" class="list-empty">暂无资金账户</view>
          <view v-for="account in funds" :key="account.id" role="button" :aria-label="account.name" :class="['account-item', { 'reorder-selected': reorderingAccountId === account.id }]" @click="handleAccountRowTap(account)" @longpress="startReorder(account)"><image class="account-icon" src="/static/prototype/funds-account.png" mode="aspectFit" /><view class="account-main"><view class="account-name-row"><text class="account-name">{{ account.name }}</text><text v-if="!account.includedInNetAsset" class="account-exclusion-badge">不计入</text></view></view><view class="account-balance"><MoneyDisplay :value="account.balanceCents" /></view><view v-if="reorderingKind === 'FUND'" class="account-reorder-action" @click.stop.prevent="handleAccountTap(account)">交换</view><text v-else class="arrow">›</text></view>
        </view>
      </view>
    </template>
    <button class="fab" aria-label="新增资产账户" @click="openCreate">＋</button><BottomNav active="assets" />
    <view v-if="createOpen" class="asset-create-backdrop" @click.self="closeCreate" @touchmove.stop.prevent>
      <view class="asset-create-modal" role="dialog" aria-modal="true" aria-label="新增资产账户">
        <view class="asset-create-handle" />
        <text class="asset-create-title">{{ createKind === 'FUND' ? '新增资产账户' : '新增信贷账户' }}</text>
        <view class="asset-type-tabs"><button :class="{ active: createKind === 'FUND' }" @click="createKind = 'FUND'">资金账户</button><button :class="{ active: createKind === 'CREDIT' }" @click="createKind = 'CREDIT'">信贷账户</button></view>
        <view class="asset-create-fields">
          <view class="asset-create-row"><text>账户名称</text><input v-model="createName" maxlength="20" :placeholder="createKind === 'CREDIT' ? '例如：xx信用卡' : '例如：微信'" aria-required="true" :disabled="savingAccount" /></view>
          <view v-if="createKind === 'FUND'" class="asset-create-row"><text>余额</text><view class="asset-create-money"><input v-model="createBalance" type="digit" inputmode="decimal" placeholder="0" aria-required="true" :disabled="savingAccount" /></view></view>
          <template v-else><view class="asset-create-row"><text>总额度</text><view class="asset-create-money"><input v-model="createCreditLimit" type="digit" inputmode="decimal" placeholder="0" aria-required="true" :disabled="savingAccount" /></view></view><view class="asset-create-row"><text>当前欠款</text><view class="asset-create-money"><input v-model="createCurrentDebt" type="digit" inputmode="decimal" placeholder="0" aria-required="true" :disabled="savingAccount" /></view></view></template>
          <view class="asset-create-row asset-create-switch"><text>计入净资产</text><switch :checked="createIncluded" aria-required="true" :disabled="savingAccount" color="#49AD9C" @change="onIncluded" /></view>
        </view>
        <view class="asset-create-actions"><button class="asset-create-cancel" :disabled="savingAccount" @click="createOpen = false">取消</button><button class="asset-create-save" :disabled="savingAccount" @click="saveAccount">{{ savingAccount ? '保存中…' : '保存账户' }}</button></view>
      </view>
    </view>
  </view>
</template>
