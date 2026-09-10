<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import BottomNav from '../../components/BottomNav.vue'
import PageHeader from '../../components/PageHeader.vue'
import TransactionRow from '../../components/TransactionRow.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import { Summary, Transaction, useLedger } from '../../stores/ledger'
import { localDateTime } from '../../utils/money'

const ledger = useLedger()
const state = ledger.state
const month = ref(localDateTime().slice(0, 7))
const loading = ref(false)
const loadError = ref(false)
const summary = ref<Summary>()
let loadSequence = 0
const groups = computed(() => {
  const map: Record<string, Transaction[]> = {}
  for (const transaction of (summary.value?.transactions || [])) (map[transaction.occurredAt.slice(0, 10)] ||= []).push(transaction)
  return Object.entries(map).sort((left, right) => right[0].localeCompare(left[0]))
})
const monthTitle = computed(() => {
  const [year, value] = month.value.split('-')
  return `${year} 年 ${Number(value)} 月`
})
const dayTotal = (rows: Transaction[], type: string) => rows.filter(row => row.type === type).reduce((total, row) => total + row.amountCents, 0)
const dateTitle = (date: string) => date.replace(/(\d{4})-(\d{2})-(\d{2})/, '$1年$2月$3日')
async function load() {
  month.value = localDateTime().slice(0, 7)
  if (!state.token) return
  const sequence = ++loadSequence
  loading.value = true
  loadError.value = false
  try {
    const result = await ledger.refresh(month.value)
    if (sequence === loadSequence) summary.value = result
  } catch {
    if (sequence === loadSequence) loadError.value = true
  } finally {
    if (sequence === loadSequence) loading.value = false
  }
}

function retry() { void load() }
function open(id: string) { uni.navigateTo({ url: `/pages/detail/detail?id=${id}` }) }
function newEntry() { uni.navigateTo({ url: '/pages/entry/entry' }) }

onShow(async () => {
  try {
    if (!state.token) {
      // #ifdef H5
      uni.reLaunch({ url: '/pages/auth/login/login' }); return
      // #endif
      // #ifdef MP-WEIXIN
      await ledger.login()
      // #endif
    }
    await load()
  } catch {
    loadError.value = true
  }
})
onPullDownRefresh(async () => { await load(); uni.stopPullDownRefresh() })
</script>

<template>
  <view class="page home-page">
    <PageHeader />
    <view class="summary-card">
      <text class="summary-kicker">{{ monthTitle }} · 日均消费</text>
      <view v-if="loading || loadError" class="summary-placeholder">{{ loading ? '正在加载收支…' : '收支暂不可用' }}</view>
      <MoneyDisplay v-else class="summary-amount" :value="summary?.dailyExpenseCents" />
      <view v-if="!loading && !loadError" class="summary-foot"><view>本月支出<MoneyDisplay :value="summary?.expenseCents" /></view><view>本月收入<MoneyDisplay class="income" :value="summary?.incomeCents" /></view></view>
    </view>
    <view class="section-row"><text class="section-title">最近记账</text></view>
    <scroll-view scroll-y :show-scrollbar="false" class="home-recent-list transaction-list" :refresher-enabled="true" :refresher-triggered="loading" @refresherrefresh="load">
      <view v-if="loading" class="list-empty">正在加载账单…</view>
      <view v-else-if="loadError" class="list-empty">暂时无法加载账单<button class="text-button" @click="retry">重试</button></view>
      <template v-else>
        <view v-if="!groups.length" class="list-empty"><view class="empty-art" />暂无记账记录<button class="text-button" @click="newEntry">添加第一笔记账</button></view>
        <view v-for="group in groups" :key="group[0]" class="date-group">
          <view class="date-heading"><text class="date-title">{{ dateTitle(group[0]) }}</text><view class="date-flow"><MoneyDisplay class="income" prefix="收 " :value="dayTotal(group[1], 'INCOME')" /><MoneyDisplay class="expense" prefix="支 " :value="dayTotal(group[1], 'EXPENSE')" /></view></view>
          <TransactionRow v-for="transaction in group[1]" :key="transaction.id" :transaction="transaction" @open="open" />
        </view>
      </template>
    </scroll-view>
    <button class="fab" aria-label="新增记账" @click="newEntry">＋</button><BottomNav active="home" />
  </view>
</template>
