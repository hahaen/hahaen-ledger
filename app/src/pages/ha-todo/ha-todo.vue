<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import CenterModal from '../../components/CenterModal.vue'
import { todoApi, type TodoAttempt, type TodoItem, type TodoStatus } from '../../utils/api'
import { registerWechatShare } from '../../utils/wechatShare'
import { useLedger } from '../../stores/ledger'

registerWechatShare()
const ledger = useLedger()
const status = ref<TodoStatus>('PENDING')
const items = ref<TodoItem[]>([])
const pendingCount = ref(0)
const completedCount = ref(0)
const page = ref(1)
const hasMore = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const loadError = ref('')
const deleteTarget = ref<TodoItem | null>(null)
const attemptTarget = ref<TodoItem | null>(null)
const attemptRows = ref<TodoAttempt[]>([])
const attemptLoading = ref(false)
const attemptError = ref('')
const actingId = ref('')
const requestKeys = new Map<string, string>()
let loadSequence = 0
const recurrenceLabel = (item: TodoItem) => {
  if (item.recurrence === 'ONCE') return '仅一次'
  if (item.recurrence === 'DAILY') return '每天'
  const day = Number(item.anchorAt.slice(8, 10))
  if (item.recurrence === 'MONTHLY') return `每月 ${day} 日`
  if (item.recurrence === 'EVERY_N_MONTHS') return `每隔 ${item.monthInterval} 月 ${day} 日`
  return `每年 ${Number(item.anchorAt.slice(5, 7))} 月 ${day} 日`
}
const niceDate = (value: string) => value ? value.slice(0, 16).replace('T', ' ') : ''
const newKey = () => `${Date.now()}-${Math.random().toString(36).slice(2)}`
function stableKey(signature: string) {
  let key = requestKeys.get(signature)
  if (!key) { key = newKey(); requestKeys.set(signature, key) }
  return key
}
function backToMine() {
  if (!actingId.value) uni.switchTab({ url: '/pages/mine/mine' })
}
async function load(reset = true) {
  if (!ledger.state.token) { uni.reLaunch({ url: '/pages/auth/login/login' }); return }
  const sequence = ++loadSequence
  if (reset) { loading.value = true; page.value = 1; loadError.value = '' }
  else { loadingMore.value = true; loadError.value = '' }
  try {
    const result = await todoApi.list(status.value, page.value)
    if (sequence !== loadSequence) return
    pendingCount.value = result.pendingCount
    completedCount.value = result.completedCount
    items.value = reset ? result.items : [...items.value, ...result.items]
    hasMore.value = result.hasMore
  } catch (error) {
    if (sequence !== loadSequence) return
    loadError.value = error instanceof Error ? error.message : '待办加载失败，请重试'
    if (!reset) page.value -= 1
  } finally {
    if (sequence === loadSequence) { loading.value = false; loadingMore.value = false }
  }
}
function switchStatus(value: TodoStatus) {
  if (status.value === value) return
  status.value = value
  items.value = []
  void load()
}
function more() { if (!loadingMore.value && hasMore.value) { page.value += 1; void load(false) } }
function openCreate() {
  uni.navigateTo({ url: '/pages/ha-todo-editor/ha-todo-editor' })
}
function openEdit(item: TodoItem) {
  uni.navigateTo({ url: `/pages/ha-todo-editor/ha-todo-editor?id=${encodeURIComponent(item.id)}` })
}
async function complete(item: TodoItem) {
  if (actingId.value) return
  actingId.value = item.id
  const signature = `complete:${item.id}`
  try {
    await todoApi.complete(item.id, stableKey(signature))
    requestKeys.delete(signature)
    uni.showToast({ title: '已完成', icon: 'none' })
    await load()
  } catch (error) { uni.showToast({ title: error instanceof Error ? error.message : '操作失败，请重试', icon: 'none' }) }
  finally { actingId.value = '' }
}
async function showAttempts(item: TodoItem) {
  attemptTarget.value = item
  attemptRows.value = []
  attemptError.value = ''
  attemptLoading.value = true
  try { attemptRows.value = await todoApi.attempts(item.id) }
  catch (error) { attemptError.value = error instanceof Error ? error.message : '记录加载失败' }
  finally { attemptLoading.value = false }
}
async function remove() {
  const item = deleteTarget.value
  if (!item || actingId.value) return
  actingId.value = item.id
  const signature = `delete:${item.id}`
  try {
    await todoApi.remove(item.id, stableKey(signature))
    requestKeys.delete(signature)
    deleteTarget.value = null
    uni.showToast({ title: '已删除', icon: 'none' })
    await load()
  } catch (error) { uni.showToast({ title: error instanceof Error ? error.message : '删除失败，请重试', icon: 'none' }) }
  finally { actingId.value = '' }
}
onShow(() => { void load() })
</script>

<template>
  <view class="page profile-page todo-page">
    <NativeNavigation variant="help" title="待办清单" back-label="返回我的" @back="backToMine" />
    <view class="profile-hero todo-hero">
      <text class="profile-eyebrow">TODO LIST</text>
      <text class="profile-hero-title">把要做的事，记在这里</text>
      <text class="profile-hero-copy">独立待办 · 北京时间提醒 · 完成记录清晰可查</text>
      <view class="profile-orbit profile-orbit-one" aria-hidden="true" /><view class="profile-orbit profile-orbit-two" aria-hidden="true" />
    </view>
    <view class="todo-summary">
      <button :class="['todo-summary-tab', { active: status === 'PENDING' }]" :aria-pressed="status === 'PENDING'" @click="switchStatus('PENDING')"><text>待完成</text><strong>{{ pendingCount }}</strong></button>
      <button :class="['todo-summary-tab', { active: status === 'COMPLETED' }]" :aria-pressed="status === 'COMPLETED'" @click="switchStatus('COMPLETED')"><text>已完成</text><strong>{{ completedCount }}</strong></button>
    </view>
    <view class="todo-section-head"><text>{{ status === 'PENDING' ? '待完成清单' : '完成记录' }}</text><button v-if="status === 'PENDING'" class="todo-add" @click="openCreate">＋ 新增待办</button></view>
    <view v-if="loading" class="profile-loading">正在加载待办…</view>
    <view v-else-if="loadError && !items.length" class="profile-error"><text>{{ loadError }}</text><button class="text-button" @click="load()">重新加载</button></view>
    <view v-else-if="!items.length" class="todo-empty"><text>{{ status === 'PENDING' ? '暂无待完成事项' : '还没有完成记录' }}</text><text>{{ status === 'PENDING' ? '点“新增待办”安排第一件事' : '完成待办后会在这里留下记录' }}</text></view>
    <view v-else class="todo-list">
      <view v-for="item in items" :key="item.id" class="todo-card">
        <view class="todo-card-main"><view class="todo-status-dot" :class="{ done: status === 'COMPLETED' }" aria-hidden="true" /><view class="todo-card-copy"><text class="todo-card-title">{{ item.title }}</text><text v-if="item.note" class="todo-card-note">{{ item.note }}</text></view></view>
        <view class="todo-meta"><text>{{ niceDate(item.dueAt) }}</text><text>{{ recurrenceLabel(item) }}</text><text v-if="item.remind" class="todo-remind">到期提醒</text></view>
        <text v-if="item.completedAt" class="todo-completed-at">完成于 {{ niceDate(item.completedAt) }}</text>
        <view class="todo-card-actions">
          <button v-if="status === 'PENDING'" :disabled="Boolean(actingId)" class="todo-action-primary" @click="complete(item)">{{ actingId === item.id ? '处理中…' : '完成' }}</button>
          <button v-if="status === 'PENDING'" :disabled="Boolean(actingId)" class="todo-action-secondary" @click="openEdit(item)">修改规则</button>
          <button v-if="item.remind" :disabled="Boolean(actingId)" class="todo-action-secondary" @click="showAttempts(item)">提醒记录</button>
          <button :disabled="Boolean(actingId)" class="todo-action-delete" @click="deleteTarget = item">删除</button>
        </view>
      </view>
    </view>
    <view v-if="loadError && items.length" class="todo-list-error"><text>{{ loadError }}</text><button class="text-button" @click="load()">重新加载</button></view>
    <button v-if="hasMore" class="todo-more" :disabled="loadingMore" @click="more">{{ loadingMore ? '加载中…' : '加载更多' }}</button>
    <text class="todo-footnote">重复待办按计划时间逐次生成；删除待完成事项会结束其重复规则，完成历史保留。</text>

    <CenterModal v-if="attemptTarget" title="提醒发送记录" @close="attemptTarget = null">
      <text v-if="attemptLoading" class="todo-attempt-state">正在加载…</text>
      <view v-else-if="attemptError" class="todo-attempt-state"><text>{{ attemptError }}</text><button class="text-button" @click="attemptTarget && showAttempts(attemptTarget)">重试</button></view>
      <text v-else-if="!attemptRows.length" class="todo-attempt-state">尚未发送提醒</text>
      <view v-for="(row, index) in attemptRows" :key="index" class="todo-attempt-row">
        <view><text>{{ row.channel === 'BARK' ? 'Bark' : 'pushplus' }}</text><text>{{ row.result === 'ACCEPTED' ? '平台已受理' : row.result === 'FAILED' ? '发送失败' : row.result === 'SKIPPED' ? '已跳过' : '发送中' }}</text></view>
        <text>{{ niceDate(row.attemptedAt) }}</text><text>{{ row.messageTitle }}</text><text>{{ row.messageBody }}</text>
      </view>
      <template #actions><view class="sheet-actions"><button class="todo-modal-primary" @click="attemptTarget = null">关闭</button></view></template>
    </CenterModal>
    <view v-if="deleteTarget" class="asset-create-backdrop" @click.self="!actingId && (deleteTarget = null)" @touchmove.stop.prevent><view class="account-delete-modal" role="dialog" aria-modal="true" aria-label="删除待办确认"><view class="asset-create-handle" /><text class="account-delete-title">删除这项待办？</text><text class="account-delete-copy">{{ deleteTarget.status === 'PENDING' ? '删除后将结束重复规则并移除所有待完成项，已完成记录保留。' : '这条完成记录将被删除。' }}</text><view class="account-delete-actions"><button class="account-delete-cancel" :disabled="Boolean(actingId)" @click="deleteTarget = null">取消</button><button class="account-delete-confirm" :disabled="Boolean(actingId)" @click="remove">{{ actingId ? '删除中…' : '删除' }}</button></view></view></view>
  </view>
</template>

<style scoped>
.todo-page { padding-bottom:calc(36px + env(safe-area-inset-bottom)); }
.todo-hero { min-height:128px; }
.todo-summary { display:flex; gap:10px; margin-top:16px; }
.todo-summary-tab { flex:1; display:flex; align-items:center; justify-content:space-between; min-height:65px; padding:13px 16px; border:1px solid #edf1ef; border-radius:17px; color:#637b73; background:#fff; text-align:left; box-shadow:0 5px 16px rgba(49,76,71,.04); }
.todo-summary-tab.active { border-color:#b9e6da; color:#236f61; background:#edf9f5; }
.todo-summary-tab text { font-size:13px; font-weight:650; }.todo-summary-tab strong { font-size:24px; line-height:1; }
.todo-section-head { display:flex; align-items:center; justify-content:space-between; margin:22px 0 12px; color:#254b43; font-size:15px; font-weight:700; }
.todo-add { display:flex; align-items:center; justify-content:center; min-height:44px; margin:0; padding:0 13px; border-radius:12px; color:#fff; background:#49ad9c; font-size:12px; font-weight:700; line-height:1.2; text-align:center; box-sizing:border-box; }
.todo-list { display:flex; flex-direction:column; gap:12px; }.todo-card { padding:16px; border:1px solid #edf1ef; border-radius:17px; background:#fff; box-shadow:0 6px 17px rgba(49,76,71,.045); }
.todo-card-main { display:flex; align-items:flex-start; gap:10px; }.todo-status-dot { flex:0 0 auto; width:19px; height:19px; margin-top:1px; border:2px solid #49ad9c; border-radius:50%; }.todo-status-dot.done { background:#49ad9c; box-shadow:inset 0 0 0 4px #fff; }
.todo-card-copy { min-width:0; flex:1; }.todo-card-title { display:block; color:#203b34; font-size:15px; font-weight:700; line-height:1.45; word-break:break-word; }.todo-card-note { display:block; margin-top:5px; color:#71857d; font-size:12px; line-height:1.55; word-break:break-word; }
.todo-meta { display:flex; flex-wrap:wrap; gap:6px; margin:12px 0 0 29px; }.todo-meta text { padding:4px 7px; border-radius:7px; color:#648378; background:#f2f7f5; font-size:10px; }.todo-meta .todo-remind { color:#237d6e; background:#e6f7f1; }
.todo-completed-at { display:block; margin:9px 0 0 29px; color:#91a49f; font-size:10px; }.todo-card-actions { display:flex; flex-wrap:wrap; gap:8px; margin-top:14px; padding-left:29px; }.todo-card-actions button { display:flex; align-items:center; justify-content:center; min-height:44px; flex:1 1 65px; margin:0; border-radius:10px; font-size:11px; font-weight:650; line-height:1.2; text-align:center; box-sizing:border-box; }.todo-action-primary { color:#fff; background:#49ad9c; }.todo-action-secondary { color:#26776a; background:#eaf7f2; }.todo-action-delete { color:#9d7469; background:#faf3f1; }
.todo-empty { display:flex; flex-direction:column; align-items:center; gap:7px; padding:45px 15px; border:1px dashed #d8e8e1; border-radius:16px; color:#6e8d81; text-align:center; }.todo-empty text:first-child { font-size:14px; font-weight:700; }.todo-empty text:last-child { font-size:11px; }
.todo-footnote { display:block; margin:20px 4px 0; color:#91a49f; font-size:10px; line-height:1.6; }.todo-more { display:flex; align-items:center; justify-content:center; width:100%; min-height:44px; margin:12px 0 0; border-radius:11px; color:#26776a; background:#eaf7f2; font-size:12px; line-height:1.2; text-align:center; }.todo-list-error { display:block; margin-top:10px; color:#b06f61; font-size:11px; }
.todo-attempt-state { display:block; padding:28px 0; color:#71857d; font-size:12px; text-align:center; }.todo-attempt-row { display:flex; flex-direction:column; gap:5px; padding:12px 0; border-bottom:1px solid #eef2f0; color:#52655f; font-size:11px; white-space:pre-wrap; word-break:break-word; }.todo-attempt-row > view { display:flex; justify-content:space-between; color:#26776a; font-weight:700; }.todo-attempt-row > text:nth-child(3) { font-weight:700; }
.todo-modal-primary { color:#fff; background:#49ad9c; }
.todo-page .account-delete-actions button, .todo-page .sheet-actions button, .todo-page .text-button { display:flex; align-items:center; justify-content:center; text-align:center; line-height:1.2; }
@media (max-width:340px) { .todo-card-actions { gap:5px; padding-left:0; }.todo-card-actions button { font-size:10px; }.todo-summary-tab { padding:12px; } }
</style>
