<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import { recurrenceLabel } from '../../utils/todoRepeat'
import { todoDueLabel } from '../../utils/todoDisplay'
import { todoApi, type TodoItem, type TodoStatus } from '../../utils/api'
import { useLedger } from '../../stores/ledger'

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
const now = ref(Date.now())
const actingId = ref('')
const requestKeys = new Map<string, string>()
let loadSequence = 0
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
function openDetail(item: TodoItem) {
  if (actingId.value) return
  uni.navigateTo({ url: `/pages/ha-todo-detail/ha-todo-detail?id=${encodeURIComponent(item.id)}` })
}
async function complete(item: TodoItem) {
  if (actingId.value || item.status !== 'PENDING') return
  actingId.value = item.id
  const signature = `complete:${item.id}`
  try {
    await todoApi.complete(item.id, stableKey(signature))
    requestKeys.delete(signature)
    item.status = 'COMPLETED'
    uni.showToast({ title: '已完成', icon: 'none' })
    await load()
  } catch (error) { uni.showToast({ title: error instanceof Error ? error.message : '操作失败，请重试', icon: 'none' }) }
  finally { actingId.value = '' }
}
onShow(() => { now.value = Date.now(); void load() })
</script>

<template>
  <view class="page profile-page todo-page">
    <NativeNavigation variant="help" :page-top-extra="20" title="待办清单" back-label="返回我的" @back="backToMine" />
    <view class="profile-hero todo-hero">
      <text class="profile-eyebrow">TODO LIST</text>
      <text class="profile-hero-title">把要做的事，记在这里</text>
      <text class="profile-hero-copy">独立待办 · 北京时间提醒 · 完成记录清晰可查</text>
      <view class="profile-orbit profile-orbit-one" aria-hidden="true" /><view class="profile-orbit profile-orbit-two" aria-hidden="true" />
    </view>
    <view class="todo-summary">
      <button :class="['todo-summary-tab', { active: status === 'PENDING' }]" :aria-pressed="status === 'PENDING'" @click="switchStatus('PENDING')"><text>待完成</text><text class="todo-summary-count">{{ pendingCount }}</text></button>
      <button :class="['todo-summary-tab', { active: status === 'COMPLETED' }]" :aria-pressed="status === 'COMPLETED'" @click="switchStatus('COMPLETED')"><text>已完成</text><text class="todo-summary-count">{{ completedCount }}</text></button>
    </view>
    <view class="todo-section-head"><text>{{ status === 'PENDING' ? '待完成清单' : '完成记录' }}</text><button v-if="status === 'PENDING'" class="todo-add" @click="openCreate">新增待办</button></view>
    <view v-if="loading" class="profile-loading">正在加载待办…</view>
    <view v-else-if="loadError && !items.length" class="profile-error"><text>{{ loadError }}</text><button class="text-button" @click="load()">重新加载</button></view>
    <view v-else-if="!items.length" class="todo-empty"><text>{{ status === 'PENDING' ? '暂无待完成事项' : '还没有完成记录' }}</text><text>{{ status === 'PENDING' ? '点“新增待办”安排第一件事' : '完成待办后会在这里留下记录' }}</text></view>
    <view v-else class="todo-list">
      <view v-for="item in items" :key="item.id" class="todo-card" @click="openDetail(item)">
        <text v-if="item.remind" class="todo-remind-badge">提醒</text>
        <button class="todo-complete-control" :disabled="Boolean(actingId) || item.status === 'COMPLETED'" :aria-label="item.status === 'COMPLETED' ? '已完成' : `完成${item.title}`" :aria-pressed="item.status === 'COMPLETED'" :loading="actingId === item.id" @click.stop="complete(item)">
          <view class="todo-status-dot" :class="{ done: item.status === 'COMPLETED' }" aria-hidden="true" />
        </button>
        <view class="todo-card-copy">
          <text class="todo-card-title">{{ item.title }}</text>
          <text v-if="item.note" class="todo-card-note">{{ item.note }}</text>
          <view class="todo-meta"><text>{{ niceDate(item.dueAt) }}</text><text>{{ recurrenceLabel(item) }}</text></view>
          <text v-if="item.completedAt" class="todo-completed-at">完成于 {{ niceDate(item.completedAt) }}</text>
        </view>
        <text class="todo-time-state" :class="{ completed: item.status === 'COMPLETED' }">{{ item.status === 'COMPLETED' ? '已完成' : todoDueLabel(item.dueAt, now) }}</text>
      </view>
    </view>
    <view v-if="loadError && items.length" class="todo-list-error"><text>{{ loadError }}</text><button class="text-button" @click="load()">重新加载</button></view>
    <button v-if="hasMore" class="todo-more" :disabled="loadingMore" @click="more">{{ loadingMore ? '加载中…' : '加载更多' }}</button>
    <text class="todo-footnote">重复待办按计划时间逐次生成；删除待完成事项会结束其重复规则，完成历史保留。</text>

  </view>
</template>

<style scoped>
.todo-page { padding-bottom:calc(36px + env(safe-area-inset-bottom)); }
.todo-hero { min-height:128px; }
.todo-summary { display:flex; gap:10px; margin-top:16px; }
.todo-summary-tab { flex:1; min-width:0; margin:0; gap:8px; display:flex; align-items:center; justify-content:space-between; min-height:65px; padding:13px 16px; border:1px solid #edf1ef; border-radius:17px; color:#637b73; background:#fff; text-align:left; box-shadow:0 5px 16px rgba(49,76,71,.04); }
.todo-summary-tab.active { border-color:#b9e6da; color:#236f61; background:#edf9f5; }
.todo-summary-tab text { font-size:13px; font-weight:650; }.todo-summary-tab .todo-summary-count { font-size:24px; line-height:1; }
.todo-section-head { display:flex; align-items:center; justify-content:space-between; margin:22px 0 12px; color:#254b43; font-size:15px; font-weight:700; }
.todo-add { display:flex; align-items:center; justify-content:center; min-height:44px; margin:0; padding:0 13px; border-radius:12px; color:#fff; background:#49ad9c; font-size:12px; font-weight:700; line-height:1.2; text-align:center; box-sizing:border-box; }
.todo-list { display:flex; flex-direction:column; gap:12px; }
.todo-card { position:relative; display:flex; align-items:center; gap:8px; padding:20px 12px 20px 8px; border:1px solid #edf1ef; border-radius:17px; background:#fff; box-shadow:0 6px 17px rgba(49,76,71,.045); }
.todo-complete-control { flex:0 0 48px; display:flex; align-items:center; justify-content:center; width:48px; min-height:48px; margin:0; padding:0; background:transparent; line-height:1; }
.todo-complete-control::after { border:0; }
.todo-status-dot { position:relative; width:20px; height:20px; border:2px solid #49ad9c; border-radius:50%; box-sizing:border-box; }
.todo-status-dot.done::after { content:''; position:absolute; left:50%; top:50%; width:10px; height:10px; border-radius:50%; background:#49ad9c; transform:translate(-50%,-50%); }
.todo-card-copy { min-width:0; flex:1; }.todo-card-title { display:block; color:#203b34; font-size:15px; font-weight:700; line-height:1.45; word-break:break-all; overflow-wrap:anywhere; }.todo-card-note { display:block; margin-top:5px; color:#71857d; font-size:12px; line-height:1.55; word-break:break-all; overflow-wrap:anywhere; }
.todo-meta { display:flex; flex-wrap:wrap; gap:6px; margin-top:10px; }.todo-meta text { min-width:0; max-width:100%; word-break:break-all; overflow-wrap:anywhere; padding:4px 7px; border-radius:7px; color:#648378; background:#f2f7f5; font-size:10px; }.todo-remind-badge { position:absolute; top:0; right:0; padding:3px 9px; border-radius:0 17px 0 8px; color:#237d6e; background:#e6f7f1; font-size:9px; line-height:1.5; }
.todo-completed-at { display:block; margin-top:9px; color:#71857d; font-size:10px; }
.todo-time-state { flex:0 0 auto; color:#a07820; background:#fff7dc; padding:5px 7px; border-radius:8px; font-size:11px; font-weight:650; white-space:nowrap; }.todo-time-state.completed { color:#237d6e; background:#e6f7f1; }
.todo-empty { display:flex; flex-direction:column; align-items:center; gap:7px; padding:45px 15px; border:1px dashed #d8e8e1; border-radius:16px; color:#6e8d81; text-align:center; }.todo-empty text:first-child { font-size:14px; font-weight:700; }.todo-empty text:last-child { font-size:11px; }
.todo-footnote { display:block; margin:20px 4px 0; color:#91a49f; font-size:10px; line-height:1.6; }.todo-more { display:flex; align-items:center; justify-content:center; width:100%; min-height:44px; margin:12px 0 0; border-radius:11px; color:#26776a; background:#eaf7f2; font-size:12px; line-height:1.2; text-align:center; }.todo-list-error { display:block; margin-top:10px; color:#b06f61; font-size:11px; }
.todo-page .account-delete-actions button, .todo-page .text-button { display:flex; align-items:center; justify-content:center; text-align:center; line-height:1.2; }
@media (max-width:340px) { .todo-card { gap:4px; padding-right:8px; }.todo-time-state { padding:5px 4px; font-size:10px; }.todo-summary-tab { padding:12px; } }
</style>
