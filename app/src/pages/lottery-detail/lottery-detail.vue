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
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import LotteryNumbers from '../../components/LotteryNumbers.vue'
import LotteryConfirm from '../../components/LotteryConfirm.vue'
import { lotteryApi } from '../../utils/api'
import { gameName, resultLabel, planLabel, deliveryLabel, type LotteryDetailKind, type LotteryRecord } from '../../utils/lottery'
const kind = ref<LotteryDetailKind>('entry'), id = ref(''), record = ref<LotteryRecord | null>(null), loading = ref(false), error = ref(''), busy = ref(false), page = ref(1)
const confirmation = ref<{ title: string; copy: string; kind: 'entry' | 'plan' | 'delivery'; id: string; action: 'delete' | 'status' | 'retry'; status?: string; key: string } | null>(null)
let sequence = 0
const title = computed(() => ({ entry: '购买详情', plan: '定投详情', batch: '批次详情', draw: '开奖详情' }[kind.value]))
const prizes = computed(() => { try { return Object.entries(JSON.parse(record.value?.prizes || '{}') as Record<string, number>).map(([grade, cents]) => ({ grade: grade.startsWith('A') ? `第${grade.slice(1)}等奖追加` : grade === '7' && record.value?.game === 'SSQ' ? '福运奖' : `第${grade}等奖`, cents })) } catch { return [] } })
function back() { if (busy.value) return; if (getCurrentPages().length > 1) uni.navigateBack(); else uni.redirectTo({ url: '/pages/lottery/lottery' }) }
async function load(more = false) {
  if (!id.value) return; const current = ++sequence; loading.value = true; error.value = ''
  try { const next = await lotteryApi.detail(kind.value, id.value, more ? page.value + 1 : 1); if (current !== sequence) return; if (more && record.value) next.items = [...(record.value.items || []), ...(next.items || [])]; record.value = next; page.value = more ? page.value + 1 : 1 }
  catch (e) { if (current === sequence) { error.value = e instanceof Error ? e.message : '加载失败，请重试'; if (!more) record.value = null } }
  finally { if (current === sequence) loading.value = false }
}
onLoad(options => { if (['entry', 'plan', 'batch', 'draw'].includes(options?.kind || '')) kind.value = options?.kind as LotteryDetailKind; id.value = options?.id || '' })
onShow(() => { void load() })
function edit() { if (!record.value) return; uni.navigateTo({ url: `/pages/lottery-editor/lottery-editor?mode=${kind.value === 'plan' ? 'plan' : 'entry'}&id=${encodeURIComponent(id.value)}` }) }
function notifications() { uni.navigateTo({ url: '/pages/notification-center/notification-center' }) }
function purchase() { uni.navigateTo({ url: `/pages/lottery-editor/lottery-editor?mode=purchase&id=${encodeURIComponent(id.value)}` }) }
function open(kind: LotteryDetailKind, id: string) { uni.navigateTo({ url: `/pages/lottery-detail/lottery-detail?kind=${kind}&id=${encodeURIComponent(id)}` }) }
function confirm(action: 'delete' | 'status' | 'retry', status?: string, deliveryId?: string) {
  confirmation.value = { title: action === 'delete' ? '删除这条记录？' : action === 'retry' ? '重新发送通知？' : status === 'ACTIVE' ? '恢复这个计划？' : status === 'ENDED' ? '结束这个计划？' : '暂停这个计划？', copy: action === 'delete' ? kind.value === 'plan' ? '将同时移除本计划全部个人批次、购买明细和统计，取消未发送提醒。官方开奖不受影响。' : '记录将从个人列表和统计中移除，未发送提醒会取消。' : action === 'retry' ? '上次请求可能已被平台受理，再次发送可能产生重复提醒。' : '暂停或结束后停止续买提醒，已购买期次仍继续核对；结束后不能恢复。', kind: deliveryId ? 'delivery' : kind.value === 'plan' ? 'plan' : 'entry', id: deliveryId || id.value, action, status, key: `lottery-${Date.now()}-${Math.random().toString(36).slice(2)}` }
}
async function execute() {
  const c = confirmation.value; if (!c || busy.value) return; busy.value = true; error.value = ''
  try { await lotteryApi.action(c.kind, c.id, c.action, c.key, c.status); confirmation.value = null; if (c.action === 'delete') { busy.value = false; back() } else await load() }
  catch (e) { error.value = e instanceof Error ? e.message : '操作失败，可重试' }
  finally { busy.value = false }
}
</script>
<template>
  <view class="page profile-page lottery-page lottery-with-actions">
    <NativeNavigation variant="help" :page-top-extra="20" :title="title" back-label="返回" @back="back" />
    <text v-if="loading && !record" class="profile-loading">正在加载…</text>
    <view v-if="error" class="lottery-notice"><text>{{ error }}</text><button class="text-button" @click="load()">重新加载</button></view>
    <template v-if="record">
      <view class="profile-hero lottery-hero"><text class="profile-eyebrow">{{ gameName(record.game) }}</text><text class="profile-hero-title">{{ record.name || (kind === 'batch' ? `本批 ${record.periods} 期` : record.issue ? `第${record.issue}期` : `第${record.position}期 · 期号待核对`) }}</text><text class="profile-hero-copy">{{ kind === 'plan' ? planLabel(record) : kind === 'entry' ? resultLabel(record) : kind === 'draw' ? record.drawDate : `起始期号 ${record.startIssue}` }}</text></view>
      <view v-if="record.red" class="lottery-card"><text class="lottery-title">{{ kind === 'draw' ? '官方开奖号码' : '购买号码' }}</text><LotteryNumbers :red="record.red" :blue="record.blue" /><text v-if="kind !== 'draw'" class="lottery-muted">{{ record.multiple }}倍{{ record.additional ? ' · 追加投注' : '' }}</text></view>
      <view v-if="record.summary" class="lottery-card lottery-summary"><view><text class="lottery-muted">已购累计成本</text><MoneyDisplay :value="record.summary.costCents" /></view><view><text class="lottery-muted">已确认税前奖金</text><MoneyDisplay :value="record.summary.prizeCents" /></view><view><text class="lottery-muted">已结算盈亏</text><MoneyDisplay :value="record.summary.profitCents" /></view><view><text class="lottery-muted">待确认期数</text><text class="lottery-title">{{ record.summary.pendingCount }}</text></view></view>
      <view v-if="kind === 'batch' && record.summary" class="lottery-card"><view class="lottery-row"><text>已开奖 {{ record.summary.drawnCount }} 期</text><text>剩余 {{ record.summary.remainingCount }} 期</text></view><text class="lottery-muted">购买日期：{{ record.purchasedOn }}</text></view>
      <view v-if="kind === 'entry'" class="lottery-card">
        <view class="lottery-row"><text>成本</text><MoneyDisplay :value="record.costCents" /></view><view class="lottery-row"><text>税前奖金</text><MoneyDisplay v-if="record.prizeCents != null" :value="record.prizeCents" /><text v-else>待确认</text></view>
        <view v-if="record.result === 'SETTLED' && record.prizeCents != null" class="lottery-row"><text>已结算盈亏</text><MoneyDisplay :value="Number(record.prizeCents) - Number(record.costCents)" /></view>
        <text class="lottery-muted">购买日期：{{ record.purchasedOn }}</text><text class="lottery-hint">{{ record.acceptedAt ? '通知渠道已受理，购买信息已锁定' : record.editable ? '尚未成功提醒，可修改购买信息' : '发送中、结果未知或定投已结算，购买信息已锁定' }}</text>
        <view v-if="record.draw" class="lottery-draw-result"><text class="lottery-title">官方开奖 · {{ record.draw.drawDate }}</text><LotteryNumbers :red="record.draw.red" :blue="record.draw.blue" /><text class="lottery-muted">{{ record.grade }}</text></view>
      </view>
      <view v-if="kind === 'draw'" class="lottery-card"><text class="lottery-title">当期单注税前奖金</text><view v-for="prize in prizes" :key="prize.grade" class="lottery-row"><text>{{ prize.grade }}</text><MoneyDisplay :value="prize.cents" /></view><text class="lottery-hint">缺失的奖级金额仍待官方补齐。{{ record.promotion ? '本期特别奖金或派奖尚待核实。' : '' }}规则：{{ record.ruleVersion }} · 修订{{ record.version }}</text><text class="lottery-hint">来源：{{ record.source }} · 同步：{{ record.syncedAt?.replace('T', ' ') }}</text></view>
      <view v-if="record.note" class="lottery-card"><text class="lottery-title">备注</text><text class="lottery-note">{{ record.note }}</text></view>
      <view v-if="kind === 'plan'" class="lottery-section"><text class="lottery-title">实际购买批次</text><button v-if="record.status === 'ACTIVE' && (!record.items?.length || record.awaitingRenewal)" class="lottery-add" @click="purchase">{{ record.items?.length ? '确认续买' : '登记购买' }}</button></view>
      <text v-if="kind === 'plan' && !record.items?.length" class="lottery-empty">还没有实际购买批次，计划不计入成本。</text>
      <button v-for="item in record.items" :key="item.id" class="lottery-card lottery-record" @click="open(kind === 'plan' ? 'batch' : 'entry', item.id)">
        <view class="lottery-row"><text class="lottery-title">{{ kind === 'plan' ? `起始 ${item.startIssue}期` : item.issue ? `第${item.issue}期` : `第${item.position}期 · 期号待核对` }}</text><text class="lottery-badge">{{ kind === 'plan' ? `${item.periods}期` : resultLabel(item) }}</text></view>
        <LotteryNumbers v-if="item.red" :red="item.red" :blue="item.blue" /><text v-if="kind === 'plan'" class="lottery-muted">实际购买于 {{ item.purchasedOn }}</text>
        <view v-else class="lottery-row"><view class="lottery-inline"><text class="lottery-muted">成本</text><MoneyDisplay :value="item.costCents" /></view><view class="lottery-inline"><text class="lottery-muted">奖金</text><MoneyDisplay v-if="item.prizeCents != null" :value="item.prizeCents" /><text v-else>待确认</text></view></view>
      </button>
      <button v-if="record.hasMore" class="secondary-btn" :disabled="loading" @click="load(true)">加载更多批次</button>
      <view v-if="record.notifications?.length" class="lottery-card"><text class="lottery-title">提醒记录</text><view v-for="event in record.notifications" :key="event.id" class="lottery-notification"><text class="lottery-title">{{ event.title }}</text><text class="lottery-note">{{ event.body }}</text><view v-for="delivery in event.deliveries" :key="delivery.id" class="lottery-row"><text>{{ delivery.channel }} · {{ deliveryLabel(delivery.status) }}</text><button v-if="delivery.status === 'FAILED' || delivery.status === 'UNKNOWN'" class="text-button" :disabled="busy" @click="confirm('retry', undefined, delivery.id)">重试</button></view><text v-if="!event.deliveries.length" class="lottery-hint">待配置通知渠道</text></view></view>
      <button v-if="!record.notificationConfigured && kind !== 'draw'" class="lottery-notice" @click="notifications">未配置通知渠道，前往通知中心设置</button>
      <view v-if="kind === 'entry' || kind === 'plan'" class="lottery-actions">
        <button v-if="kind === 'plan' || record.editable" class="secondary-btn" :disabled="busy" @click="edit">修改</button>
        <button v-if="kind === 'plan' && record.status !== 'ENDED'" class="secondary-btn" :disabled="busy" @click="confirm('status', record.status === 'PAUSED' ? 'ACTIVE' : 'PAUSED')">{{ record.status === 'PAUSED' ? '恢复' : '暂停' }}</button>
        <button v-if="kind === 'plan' && record.status !== 'ENDED'" class="secondary-btn" :disabled="busy" @click="confirm('status', 'ENDED')">结束</button>
        <button v-if="kind === 'plan' || !record.planId" class="danger-btn" :disabled="busy" @click="confirm('delete')">删除</button>
      </view>
    </template>
    <LotteryConfirm v-if="confirmation" :title="confirmation.title" :copy="confirmation.copy" :busy="busy" @cancel="confirmation = null" @confirm="execute"><text v-if="error" class="lottery-error">{{ error }}</text></LotteryConfirm>
  </view>
</template>
