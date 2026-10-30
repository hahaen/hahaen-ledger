<script lang="ts">
// #ifdef MP-WEIXIN
import { createWechatShareMessage } from '../../utils/wechatShare'
// #endif
export default {
// #ifdef MP-WEIXIN
  onShareAppMessage: createWechatShareMessage,
// #endif
}
</script>
<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import LotteryNumbers from '../../components/LotteryNumbers.vue'
import { lotteryApi } from '../../utils/api'
import { gameName, resultLabel, planLabel, type LotteryGame, type LotteryKind, type LotteryPage, type LotteryRecord } from '../../utils/lottery'
const game = ref<LotteryGame>('DLT'), kind = ref<LotteryKind>('plan')
const data = ref<LotteryPage | null>(null), items = ref<LotteryRecord[]>([]), loading = ref(false), error = ref(''), page = ref(1)
let sequence = 0
const back = () => uni.switchTab({ url: '/pages/mine/mine' })
async function load(reset = true) {
  const current = ++sequence; loading.value = true; error.value = ''
  try { const next = await lotteryApi.list(game.value, kind.value, reset ? 1 : page.value + 1); if (current !== sequence) return; data.value = next; items.value = reset ? next.items : [...items.value, ...next.items]; page.value = reset ? 1 : page.value + 1 }
  catch (e) { if (current === sequence) { error.value = e instanceof Error ? e.message : '加载失败，请重试'; if (reset) { data.value = null; items.value = [] } } }
  finally { if (current === sequence) loading.value = false }
}
function chooseGame(value: LotteryGame) { if (game.value !== value) { game.value = value; data.value = null; items.value = []; void load() } }
function chooseKind(value: LotteryKind) { if (kind.value !== value) { kind.value = value; items.value = []; void load() } }
const create = () => uni.navigateTo({ url: `/pages/lottery-editor/lottery-editor?mode=${kind.value}&game=${game.value}` })
const detail = (item: LotteryRecord) => uni.navigateTo({ url: `/pages/lottery-detail/lottery-detail?kind=${kind.value === 'single' ? 'entry' : 'plan'}&id=${encodeURIComponent(item.id)}` })
const draws = () => uni.navigateTo({ url: `/pages/lottery-draws/lottery-draws?game=${game.value}` })
const notifications = () => uni.navigateTo({ url: '/pages/notification-center/notification-center' })
onShow(() => { void load() })
</script>
<template>
  <view class="page profile-page lottery-page">
    <NativeNavigation variant="help" :page-top-extra="20" title="彩票" back-label="返回我的" @back="back" />
    <view class="lottery-tabs"><button :class="{ active: game === 'DLT' }" :aria-pressed="game === 'DLT'" @click="chooseGame('DLT')">大乐透 · 体彩</button><button :class="{ active: game === 'SSQ' }" :aria-pressed="game === 'SSQ'" @click="chooseGame('SSQ')">双色球 · 福彩</button></view>
    <view v-if="data" class="lottery-card lottery-summary">
      <view><text class="lottery-muted">已购累计成本</text><MoneyDisplay :value="data.summary.costCents" /></view><view><text class="lottery-muted">已确认税前奖金</text><MoneyDisplay :value="data.summary.prizeCents" /></view>
      <view><text class="lottery-muted">已结算盈亏</text><MoneyDisplay :value="data.summary.profitCents" /></view><view><text class="lottery-muted">尚未开奖成本</text><MoneyDisplay :value="data.summary.waitingCostCents" /></view>
      <view class="lottery-summary-note"><text>已结算成本：</text><MoneyDisplay :value="data.summary.settledCostCents" /><text> · {{ data.summary.pendingCount }}期尚待确认</text></view>
    </view>
    <button v-if="data && !data.notificationConfigured" class="lottery-notice" @click="notifications">尚未配置通知渠道，前往通知中心设置</button>
    <view class="lottery-section"><view class="lottery-tabs lottery-kind-tabs"><button :class="{ active: kind === 'single' }" @click="chooseKind('single')">单次购买</button><button :class="{ active: kind === 'plan' }" @click="chooseKind('plan')">定投计划</button></view><button class="lottery-add" @click="create">{{ kind === 'single' ? '添加记录' : '新建计划' }}</button></view>
    <text v-if="loading && !items.length" class="profile-loading">正在加载…</text>
    <view v-else-if="error" class="lottery-notice"><text>{{ error }}</text><button class="text-button" @click="load()">重新加载</button></view>
    <view v-else-if="!items.length" class="lottery-empty"><text>暂无{{ kind === 'single' ? '购买记录' : '定投计划' }}</text><text>在这里记录已经购买的彩票，或保存长期号码计划</text></view>
    <button v-for="item in items" :key="item.id" class="lottery-card lottery-record" @click="detail(item)">
      <view class="lottery-row"><text class="lottery-title">{{ kind === 'plan' ? item.name : `第${item.issue}期` }}</text><text class="lottery-badge">{{ kind === 'plan' ? planLabel(item) : resultLabel(item) }}</text></view>
      <LotteryNumbers :red="item.red" :blue="item.blue" />
      <view class="lottery-row lottery-muted"><text>{{ item.multiple }}倍{{ item.additional ? ' · 追加' : '' }}</text><text v-if="kind === 'single'">{{ item.purchasedOn }}</text></view>
      <view class="lottery-row"><view class="lottery-inline"><text class="lottery-muted">成本</text><MoneyDisplay :value="kind === 'plan' ? item.summary?.costCents : item.costCents" /></view><view class="lottery-inline"><text class="lottery-muted">奖金</text><MoneyDisplay v-if="kind === 'plan' || item.prizeCents != null" :value="kind === 'plan' ? item.summary?.prizeCents : item.prizeCents" /><text v-else class="lottery-muted">待确认</text></view></view>
    </button>
    <button v-if="data?.hasMore && !error" class="secondary-btn" :disabled="loading" @click="load(false)">{{ loading ? '加载中…' : '加载更多' }}</button>
    <button class="text-button" @click="draws">查看{{ gameName(game) }}历史开奖</button>
    <text v-if="data?.sync.lastSuccessAt" class="lottery-footnote">官方数据同步于 {{ data.sync.lastSuccessAt.replace('T', ' ') }}{{ data.sync.lastError ? ' · 最近同步异常，保留本地数据' : '' }}</text>
    <text class="lottery-footnote">仅登记实际购买，不自动购票；奖金均为税前核对结果，兑奖以官方公告及票面为准。</text>
  </view>
</template>
