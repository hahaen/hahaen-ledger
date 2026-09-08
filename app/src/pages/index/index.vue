<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import BottomNav from '../../components/BottomNav.vue'
import PageHeader from '../../components/PageHeader.vue'
import TransactionRow from '../../components/TransactionRow.vue'
import MonthPicker from '../../components/MonthPicker.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import { Transaction, useLedger } from '../../stores/ledger'
import { localDateTime } from '../../utils/money'

const ledger = useLedger()
const state = ledger.state
const month = ref(localDateTime().slice(0, 7))
const loading = ref(false)
const monthOpen = ref(false)
const loadError = ref(false)
const groups = computed(() => {
  const map: Record<string, Transaction[]> = {}
  for (const transaction of state.transactions) (map[transaction.occurredAt.slice(0, 10)] ||= []).push(transaction)
  return Object.entries(map).sort((left, right) => right[0].localeCompare(left[0]))
})
const monthTitle = computed(() => {
  const [year, value] = month.value.split('-')
  return `${year} 年 ${Number(value)} 月`
})
const dayTotal = (rows: Transaction[], type: string) => rows.filter(row => row.type === type).reduce((total, row) => total + row.amountCents, 0)
const dateTitle = (date: string) => date.replace(/(\d{4})-(\d{2})-(\d{2})/, '$1年$2月$3日')
async function load() {
  if (!state.token) return
  loading.value = true
  loadError.value = false
  try {
    await ledger.refresh(month.value)
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

function retry() { void load() }
function open(id: string) { uni.navigateTo({ url: `/pages/detail/detail?id=${id}` }) }
function newEntry() { uni.navigateTo({ url: '/pages/entry/entry' }) }

onShow(async () => {
  try {
    if (!state.token) await ledger.login()
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
      <button class="summary-kicker" aria-label="选择月份" @click="monthOpen = true">{{ monthTitle }} · 日均消费</button>
      <MoneyDisplay class="summary-amount" :value="state.summary?.dailyExpenseCents" />
      <view class="summary-foot"><view>本月支出<MoneyDisplay :value="state.summary?.expenseCents" /></view><view>本月收入<MoneyDisplay class="income" :value="state.summary?.incomeCents" /></view></view>
    </view>
    <view class="section-row"><text class="section-title">最近记账</text></view>
    <scroll-view scroll-y :show-scrollbar="false" class="home-recent-list transaction-list">
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
    <MonthPicker v-if="monthOpen" :value="month" @close="monthOpen = false" @select="month = $event; monthOpen = false; load()" />
  </view>
</template>
