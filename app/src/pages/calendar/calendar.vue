<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import BottomNav from '../../components/BottomNav.vue'
import PageHeader from '../../components/PageHeader.vue'
import TransactionRow from '../../components/TransactionRow.vue'
import MonthPicker from '../../components/MonthPicker.vue'
import { request } from '../../utils/api'
import { Summary, Transaction, TransactionType, useLedger } from '../../stores/ledger'
import { localDateTime, yuan } from '../../utils/money'

type CalendarDay = { date: string; day: number; currentMonth: boolean; today: boolean; hasRecords: boolean; expenseCents: number; incomeCents: number; balanceCents: number }
type CalendarMonth = { month: string; days: CalendarDay[] }
type DayDetail = { date: string; expenseCents: number; incomeCents: number; balanceCents: number; transactions: Transaction[] }

const ledger = useLedger()
const cursor = ref(new Date())
const selected = ref(localDateTime().slice(0, 10))
const days = ref<CalendarDay[]>([])
const detail = ref<DayDetail | null>(null)
const loading = ref(false)
const error = ref(false)
const dayError = ref(false)
const monthOpen = ref(false)
const monthTransactions = ref<Transaction[]>([])
const typeOrder: TransactionType[] = ['EXPENSE', 'INCOME', 'TRANSFER', 'REPAYMENT']
const dayTypes = (date: string) => typeOrder.filter(type => monthTransactions.value.some(row => row.occurredAt.startsWith(date) && row.type === type))
const dotClass = (type: TransactionType) => ({ EXPENSE: 'expense-dot', INCOME: 'income-dot', TRANSFER: 'neutral-dot', REPAYMENT: 'repayment-dot' }[type])
let requestSequence = 0
const monthKey = computed(() => `${cursor.value.getFullYear()}-${String(cursor.value.getMonth() + 1).padStart(2, '0')}`)
const monthTitle = computed(() => `${cursor.value.getFullYear()} 年 ${cursor.value.getMonth() + 1} 月`)
const weekLabels = ['日', '一', '二', '三', '四', '五', '六']
const visibleDays = computed(() => { const last = days.value.map(day => day.currentMonth).lastIndexOf(true); return days.value.slice(0, Math.ceil((last + 1) / 7) * 7) })
const selectedTitle = computed(() => selected.value ? `${Number(selected.value.slice(5, 7))}月${Number(selected.value.slice(8, 10))}日` : '当天')
const selectedWeek = computed(() => selected.value ? `周${weekLabels[new Date(`${selected.value}T00:00:00`).getDay()]}` : '')
async function loadMonth() {
  if (!ledger.state.token) return
  const sequence = ++requestSequence
  loading.value = true
  error.value = false
  try {
    const [result, summary] = await Promise.all([
      request<CalendarMonth>(`/api/app/calendar?year=${monthKey.value.slice(0, 4)}&month=${monthKey.value.slice(5, 7)}`),
      request<Summary>(`/api/app/home/summary?month=${monthKey.value}`),
    ])
    if (sequence !== requestSequence) return
    days.value = result.days
    monthTransactions.value = summary.transactions
    const current = result.days.filter(day => day.currentMonth)
    const today = localDateTime().slice(0, 10)
    selected.value = current.some(day => day.date === today) ? today : current.find(day => day.hasRecords)?.date || current[0]?.date || ''
    await loadDay(selected.value, sequence)
  } catch {
    if (sequence === requestSequence) error.value = true
  } finally {
    if (sequence === requestSequence) loading.value = false
  }
}

async function loadDay(date: string, sequence = requestSequence) {
  if (!date) return
  dayError.value = false
  try {
    const result = await request<DayDetail>(`/api/app/calendar/${date}`)
    if (sequence === requestSequence && selected.value === date) detail.value = result
  } catch {
    if (sequence === requestSequence && selected.value === date) { detail.value = null; dayError.value = true }
  }
}

function moveMonth(delta: number) {
  cursor.value = new Date(cursor.value.getFullYear(), cursor.value.getMonth() + delta, 1)
  void loadMonth()
}
function today() { cursor.value = new Date(); void loadMonth() }
function selectDay(day: CalendarDay) {
  if (!day.currentMonth) { cursor.value = new Date(`${day.date}T00:00:00`); void loadMonth(); return }
  selected.value = day.date
  void loadDay(day.date)
}
function open(id: number) { uni.navigateTo({ url: `/pages/detail/detail?id=${id}` }) }

onShow(() => { void loadMonth() })
</script>

<template>
  <view class="page calendar-page">
    <PageHeader subtitle="按日期回看每一笔生活" />
    <view class="calendar-card">
      <view class="calendar-toolbar"><button class="calendar-month" aria-label="选择月份" @click="monthOpen = true">{{ monthTitle }}</button><view class="calendar-controls"><button aria-label="上个月" @click="moveMonth(-1)">‹</button><button class="calendar-today" @click="today">今天</button><button aria-label="下个月" @click="moveMonth(1)">›</button></view></view>
      <view class="week-row"><text v-for="label in weekLabels" :key="label">{{ label }}</text></view>
      <view v-if="loading" class="list-empty">正在加载月历…</view>
      <view v-else-if="error" class="list-empty">暂时无法加载月历<button class="text-button" @click="loadMonth">重试</button></view>
      <view v-else class="calendar-grid"><button v-for="day in visibleDays" :key="day.date" :class="['calendar-cell', { muted: !day.currentMonth, selected: selected === day.date, today: day.today }]" :disabled="!day.currentMonth" :aria-label="day.date" :aria-pressed="selected === day.date" @click="selectDay(day)"><text class="day-num">{{ day.day }}</text><view class="calendar-dots"><text v-for="type in dayTypes(day.date)" :key="type" :class="['calendar-dot', dotClass(type)]" /></view></button></view>
      <view class="calendar-legend"><view><text class="calendar-dot expense-dot" />支出</view><view><text class="calendar-dot income-dot" />收入</view><view><text class="calendar-dot neutral-dot" />转账</view><view><text class="calendar-dot repayment-dot" />还款</view></view>
      <view class="day-summary"><view><text>当日支出</text><text class="day-total expense">{{ yuan(detail?.expenseCents) }}</text></view><view><text>当日收入</text><text class="day-total income">{{ yuan(detail?.incomeCents) }}</text></view><view><text>当日结余</text><text class="day-total">{{ yuan(detail?.balanceCents) }}</text></view></view>
    </view>
    <view class="date-heading calendar-date"><view><text class="date-title">{{ selectedTitle }}</text><text class="date-week">{{ selectedWeek }}</text></view><text class="section-meta">{{ detail?.transactions.length || 0 }} 笔</text></view>
    <view class="transaction-list"><view v-if="dayError" class="list-empty">暂时无法加载当天账单<button class="text-button" @click="loadDay(selected)">重试</button></view><view v-else-if="!detail?.transactions.length" class="list-empty">这一天还没有记账记录</view><template v-else><TransactionRow v-for="transaction in detail.transactions" :key="transaction.id" :transaction="transaction" @open="open" /></template></view>
    <BottomNav active="calendar" />
    <MonthPicker v-if="monthOpen" :value="monthKey" @close="monthOpen = false" @select="cursor = new Date($event + '-01T00:00:00'); monthOpen = false; loadMonth()" />
  </view>
</template>
