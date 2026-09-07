<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import BottomNav from '../../components/BottomNav.vue'
import PageHeader from '../../components/PageHeader.vue'
import CenterModal from '../../components/CenterModal.vue'
import { request } from '../../utils/api'
import { Account, AssetOverview, useLedger } from '../../stores/ledger'
import { yuan } from '../../utils/money'

const ledger = useLedger()
const state = ledger.state
const overview = ref<AssetOverview | null>(null)
const loading = ref(false)
const choosingKind = ref(false)
const error = ref(false)
const fundsExpanded = ref(true)
const creditsExpanded = ref(true)
const funds = computed(() => (overview.value?.accounts || state.accounts).filter(account => account.kind === 'FUND' && account.status === 'ACTIVE'))
const credits = computed(() => (overview.value?.accounts || state.accounts).filter(account => account.kind === 'CREDIT' && account.status === 'ACTIVE'))

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
function open(id: number) { uni.navigateTo({ url: `/pages/account/account?id=${id}` }) }
function add(kind: 'FUND' | 'CREDIT') { uni.navigateTo({ url: `/pages/account/account?kind=${kind}` }) }
function retry() { void load() }
function accountBalance(account: Account) { return yuan(account.balanceCents) }

onShow(() => { void load() })
</script>

<template>
  <view class="page assets-page">
    <PageHeader />
    <view class="asset-hero"><text class="asset-label">净资产</text><view class="asset-net"><text class="currency">¥</text>{{ yuan(overview?.netAssetsCents).slice(1) }}</view><view class="asset-breakdown"><view><text>总资产</text><text class="breakdown-value">{{ yuan(overview?.totalAssetsCents) }}</text></view><view><text>总负债</text><text class="breakdown-value liability">{{ yuan(overview?.totalLiabilitiesCents) }}</text></view></view></view>
    <view v-if="loading" class="card empty">正在加载资产…</view>
    <view v-else-if="error" class="card empty">暂时无法加载资产<button class="text-button" @click="retry">重试</button></view>
    <template v-else>
      <view class="account-section">
        <button class="account-heading" :aria-expanded="creditsExpanded" @click="creditsExpanded = !creditsExpanded"><text>信贷账户</text><view class="account-toggle">{{ creditsExpanded ? '收起' : '展开' }}<text :class="['toggle-chevron', { collapsed: !creditsExpanded }]" /></view></button>
        <view v-if="creditsExpanded" class="account-list">
          <view v-if="!credits.length" class="list-empty">暂无信贷账户</view>
          <button v-for="account in credits" :key="account.id" class="account-item" @click="open(account.id)"><image class="account-icon credit" src="/static/prototype/credit-account.png" mode="aspectFit" /><view class="account-main"><text class="account-name">{{ account.name }}</text><text class="credit-available">可用 {{ yuan(account.creditLimitCents - account.balanceCents) }}</text></view><view class="account-balance liability"><text class="balance-caption">欠款</text>{{ accountBalance(account) }}</view><text class="arrow">›</text></button>
        </view>
      </view>
      <view class="account-section">
        <button class="account-heading" :aria-expanded="fundsExpanded" @click="fundsExpanded = !fundsExpanded"><text>资金账户</text><view class="account-toggle">{{ fundsExpanded ? '收起' : '展开' }}<text :class="['toggle-chevron', { collapsed: !fundsExpanded }]" /></view></button>
        <view v-if="fundsExpanded" class="account-list">
          <view v-if="!funds.length" class="list-empty">暂无资金账户</view>
          <button v-for="account in funds" :key="account.id" class="account-item" @click="open(account.id)"><image class="account-icon" src="/static/prototype/funds-account.png" mode="aspectFit" /><view class="account-main"><text class="account-name">{{ account.name }}</text></view><text class="account-balance">{{ accountBalance(account) }}</text><text class="arrow">›</text></button>
        </view>
      </view>
    </template>
    <button class="fab" aria-label="新增资产账户" @click="choosingKind = true">＋</button><BottomNav active="assets" />
    <CenterModal v-if="choosingKind" title="新增账户" @close="choosingKind = false"><button class="account-item" @click="choosingKind = false; add('FUND')"><image class="account-icon" src="/static/prototype/funds-account.png" /><view class="account-main"><text class="account-name">资金账户</text><text class="account-desc">现金、微信、银行卡等</text></view><text class="arrow">›</text></button><button class="account-item" @click="choosingKind = false; add('CREDIT')"><image class="account-icon" src="/static/prototype/credit-account.png" /><view class="account-main"><text class="account-name">信贷账户</text><text class="account-desc">信用卡、花呗等</text></view><text class="arrow">›</text></button></CenterModal>
  </view>
</template>
