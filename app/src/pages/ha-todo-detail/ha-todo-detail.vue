<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import { todoApi, type TodoItem } from '../../utils/api'
import { recurrenceLabel } from '../../utils/todoRepeat'
import { todoDueLabel } from '../../utils/todoDisplay'
import { useLedger } from '../../stores/ledger'
import { registerWechatShare } from '../../utils/wechatShare'

const ledger = useLedger()
registerWechatShare()
const id = ref('')
const item = ref<TodoItem | null>(null)
const loading = ref(true)
const loadError = ref('')
const deleting = ref(false)
const deleteOpen = ref(false)
const deleteError = ref('')
const now = ref(Date.now())
let deleteKey = ''
const niceDate = (value: string) => value.slice(0, 16).replace('T', ' ')
function back() {
  if (deleting.value) return
  const pages = getCurrentPages()
  if (pages.length < 2 || pages[pages.length - 2]?.route !== 'pages/ha-todo/ha-todo') {
    uni.redirectTo({ url: '/pages/ha-todo/ha-todo' }); return
  }
  uni.navigateBack({ delta: 1, fail: () => uni.redirectTo({ url: '/pages/ha-todo/ha-todo' }) })
}
async function loadDetail() {
  if (!id.value || deleting.value) return
  loading.value = true
  loadError.value = ''
  try { item.value = await todoApi.detail(id.value); now.value = Date.now() }
  catch (error) { loadError.value = error instanceof Error ? error.message : '待办加载失败，请重试' }
  finally { loading.value = false }
}
function edit() {
  if (!item.value || item.value.status !== 'PENDING' || deleting.value) return
  uni.navigateTo({ url: `/pages/ha-todo-editor/ha-todo-editor?id=${encodeURIComponent(id.value)}` })
}
async function remove() {
  if (!item.value || deleting.value || !deleteOpen.value) return
  deleting.value = true
  deleteError.value = ''
  if (!deleteKey) deleteKey = `${Date.now()}-${Math.random().toString(36).slice(2)}`
  try {
    await todoApi.remove(id.value, deleteKey)
    deleteOpen.value = false
    uni.showToast({ title: '已删除', icon: 'none' })
    uni.redirectTo({ url: '/pages/ha-todo/ha-todo' })
  } catch (error) { deleteError.value = error instanceof Error ? error.message : '删除失败，请重试' }
  finally { deleting.value = false }
}
onLoad(options => {
  if (!ledger.state.token) { uni.reLaunch({ url: '/pages/auth/login/login' }); return }
  if (!options?.id || !/^\d+$/.test(options.id)) { loading.value = false; loadError.value = '待办链接无效'; return }
  id.value = options.id
})
onShow(() => { void loadDetail() })
</script>

<template>
  <view class="page profile-page todo-detail-page">
    <NativeNavigation variant="help" title="待办详情" back-label="返回待办清单" @back="back" />
    <view v-if="loading" class="profile-loading">正在加载待办…</view>
    <view v-else-if="loadError" class="profile-error"><text>{{ loadError }}</text><button v-if="id" class="text-button" @click="loadDetail">重新加载</button></view>
    <template v-else-if="item">
      <view class="todo-detail-card">
        <view class="todo-detail-heading"><text class="todo-detail-title">{{ item.title }}</text><text class="todo-detail-status" :class="{ completed: item.status === 'COMPLETED' }">{{ item.status === 'COMPLETED' ? '已完成' : todoDueLabel(item.dueAt, now) }}</text></view>
        <view class="todo-detail-field"><text>计划时间</text><text>{{ niceDate(item.dueAt) }}</text></view>
        <view class="todo-detail-field"><text>重复规则</text><text>{{ recurrenceLabel(item) }}</text></view>
        <view class="todo-detail-field"><text>到期提醒</text><text>{{ item.remind ? '已开启' : '未开启' }}</text></view>
        <view v-if="item.completedAt" class="todo-detail-field"><text>完成时间</text><text>{{ niceDate(item.completedAt) }}</text></view>
        <view v-if="item.note" class="todo-detail-note"><text>备注</text><text>{{ item.note }}</text></view>
      </view>
      <view class="profile-actions todo-detail-actions">
        <button v-if="item.status === 'PENDING'" class="todo-detail-edit" :disabled="deleting" @click="edit">修改规则</button>
        <button class="todo-detail-delete" :disabled="deleting" @click="deleteOpen = true; deleteError = ''">删除</button>
      </view>
    </template>
    <view v-if="deleteOpen && item" class="asset-create-backdrop" @click.self="!deleting && (deleteOpen = false)" @touchmove.stop.prevent>
      <view class="account-delete-modal" role="dialog" aria-modal="true" aria-label="删除待办确认">
        <view class="asset-create-handle" /><text class="account-delete-title">删除这项待办？</text>
        <text class="account-delete-copy">{{ item.status === 'PENDING' ? '删除后将结束重复规则并移除所有待完成项，已完成记录保留。' : '这条完成记录将被删除。' }}</text>
        <text v-if="deleteError" class="todo-delete-error">{{ deleteError }}</text>
        <view class="account-delete-actions"><button class="account-delete-cancel" :disabled="deleting" @click="deleteOpen = false">取消</button><button class="account-delete-confirm" :disabled="deleting" @click="remove">{{ deleting ? '删除中…' : '删除' }}</button></view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.todo-detail-page { padding-bottom:calc(110px + env(safe-area-inset-bottom)); }
.todo-detail-card { padding:20px 16px; border:1px solid #edf1ef; border-radius:18px; background:#fff; box-shadow:0 7px 18px rgba(49,76,71,.05); }
.todo-detail-heading { display:flex; flex-wrap:wrap; align-items:center; gap:12px; margin-bottom:20px; }.todo-detail-title { flex:1 1 160px; min-width:0; color:#203b34; font-size:20px; font-weight:700; word-break:break-all; overflow-wrap:anywhere; }
.todo-detail-status { flex:0 0 auto; padding:5px 8px; border-radius:8px; color:#a07820; background:#fff7dc; font-size:12px; white-space:nowrap; }.todo-detail-status.completed { color:#237d6e; background:#e6f7f1; }
.todo-detail-field { display:flex; justify-content:space-between; gap:16px; padding:14px 0; border-top:1px solid #edf1ef; font-size:13px; line-height:1.6; }.todo-detail-field > text:first-child { flex-shrink:0; color:#71857d; }.todo-detail-field > text:last-child { min-width:0; flex:1; word-break:break-all; color:#203b34; text-align:right; overflow-wrap:anywhere; }
.todo-detail-note { padding-top:16px; border-top:1px solid #edf1ef; font-size:13px; line-height:1.7; }.todo-detail-note text { display:block; color:#71857d; }.todo-detail-note text:last-child { word-break:break-all; margin-top:8px; color:#203b34; white-space:pre-wrap; overflow-wrap:anywhere; }
.todo-detail-actions { gap:12px; }.todo-detail-actions button { display:flex; align-items:center; justify-content:center; flex:1; min-width:0; min-height:48px; margin:0; padding:0; border-radius:13px; font-size:14px; font-weight:700; line-height:1.2; }.todo-detail-edit { color:#26776a; background:#eaf7f2; }.todo-detail-delete { color:#d47769; background:#fff3f0; }
.todo-delete-error { display:block; margin-top:12px; color:#d47769; font-size:12px; }.todo-detail-page .account-delete-actions button, .todo-detail-page .text-button { display:flex; align-items:center; justify-content:center; line-height:1.2; }
</style>
