<script setup lang="ts">
import { ref } from 'vue'
import { onBackPress, onLoad } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import EntryDateTimePicker from '../../components/EntryDateTimePicker.vue'
import TodoRepeatFieldsInput from '../../components/TodoRepeatFields.vue'
import { cleanRepeat, repeatError } from '../../utils/todoRepeat'
import { todoApi, type TodoPayload, type TodoRepeatFields } from '../../utils/api'
import { useLedger } from '../../stores/ledger'
import { registerWechatShare } from '../../utils/wechatShare'

const ledger = useLedger()
registerWechatShare()
const editId = ref('')
const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
const discardOpen = ref(false)
const pickerMode = ref<'date' | 'time' | ''>('')
const title = ref('')
const note = ref('')
const date = ref('')
const time = ref('')
const remind = ref(true)
const customRepeat = ref<TodoRepeatFields>({ repeatMode: 'TIME', repeatUnit: 'DAY', repeatInterval: 1, weekDays: '', monthDays: '', yearDays: '', fixedDates: '', lastDay: false })
const initialValue = ref('')
const requestKeys = new Map<string, string>()
let allowBack = false

const beijingTime = (offsetMs = 0) => new Date(Date.now() + offsetMs + 8 * 60 * 60 * 1000).toISOString().slice(0, 16)
const snapshot = () => JSON.stringify([title.value, note.value, date.value, time.value, remind.value, customRepeat.value])
const newKey = () => `${Date.now()}-${Math.random().toString(36).slice(2)}`
function stableKey(signature: string) {
  let key = requestKeys.get(signature)
  if (!key) { key = newKey(); requestKeys.set(signature, key) }
  return key
}
function returnToList() {
  allowBack = true
  const pages = getCurrentPages()
  const previous = pages.length > 1 ? pages[pages.length - 2]?.route : ''
  if (previous !== 'pages/ha-todo/ha-todo' && previous !== 'pages/ha-todo-detail/ha-todo-detail') {
    uni.redirectTo({ url: '/pages/ha-todo/ha-todo' })
    return
  }
  uni.navigateBack({ delta: 1, fail: () => uni.redirectTo({ url: '/pages/ha-todo/ha-todo' }) })
}
function returnAfterSave() {
  allowBack = true
  const pages = getCurrentPages()
  const previous = pages.length > 1 ? pages[pages.length - 2]?.route : ''
  // 修改规则会替换未完成发生项，保存后不能再加载旧详情 ID。
  const delta = previous === 'pages/ha-todo-detail/ha-todo-detail' ? 2 : 1
  if (pages[pages.length - 1 - delta]?.route === 'pages/ha-todo/ha-todo') {
    uni.navigateBack({ delta, fail: () => uni.redirectTo({ url: '/pages/ha-todo/ha-todo' }) })
  } else {
    uni.redirectTo({ url: '/pages/ha-todo/ha-todo' })
  }
}
function back() {
  if (saving.value) return
  if (pickerMode.value) { pickerMode.value = ''; return }
  if (editId.value && !loading.value && !loadError.value && snapshot() !== initialValue.value) discardOpen.value = true
  else returnToList()
}
onBackPress(() => { if (allowBack) return false; back(); return true })

function initializeCreate() {
  const next = beijingTime(60 * 60 * 1000)
  date.value = next.slice(0, 10)
  time.value = next.slice(11, 16)
  initialValue.value = snapshot()
  loading.value = false
}
async function loadDetail() {
  if (!editId.value) return
  loading.value = true
  loadError.value = ''
  try {
    const item = await todoApi.detail(editId.value)
    title.value = item.title
    note.value = item.note || ''
    const next = item.dueAt.slice(0, 16) > beijingTime() ? item.dueAt.slice(0, 16) : beijingTime(60 * 60 * 1000)
    date.value = next.slice(0, 10)
    time.value = next.slice(11, 16)
    if (item.recurrence !== 'ONCE' && item.recurrence !== 'CUSTOM') {
      const monthly = item.recurrence === 'MONTHLY' || item.recurrence === 'EVERY_N_MONTHS'
      customRepeat.value = { repeatMode: 'TIME', repeatUnit: monthly ? 'MONTH' : item.recurrence === 'YEARLY' ? 'YEAR' : 'DAY',
        repeatInterval: item.recurrence === 'EVERY_N_MONTHS' ? item.monthInterval : 1,
        monthDays: monthly ? String(Number(item.anchorAt.slice(8, 10))) : '',
        yearDays: item.recurrence === 'YEARLY' ? item.anchorAt.slice(5, 10) : '' }
    }
    remind.value = item.remind
    if (item.recurrence === 'CUSTOM') customRepeat.value = {
      repeatMode: item.repeatMode, repeatUnit: item.repeatUnit || 'DAY', repeatInterval: item.repeatInterval || 1,
      weekDays: item.weekDays || '', monthDays: item.monthDays || '', lastDay: item.lastDay,
      yearDays: item.yearDays || '', fixedDates: item.fixedDates || '' }

    initialValue.value = snapshot()
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '待办加载失败，请重试'
  } finally { loading.value = false }
}
onLoad((options) => {
  if (!ledger.state.token) { uni.reLaunch({ url: '/pages/auth/login/login' }); return }
  const id = options?.id
  if (!id) { initializeCreate(); return }
  if (!/^\d+$/.test(id)) { loadError.value = '待办链接无效'; loading.value = false; return }
  editId.value = id
  void loadDetail()
})

function chooseRemind(event: Event) {
  remind.value = Boolean((event as Event & { detail?: { value?: unknown } }).detail?.value)
}
function selectPicker(value: string) {
  if (pickerMode.value === 'date') date.value = value
  else time.value = value
  pickerMode.value = ''
}
function payload(): Omit<TodoPayload, 'idempotencyKey'> | null {
  const cleanTitle = title.value.trim()
  if (!cleanTitle || cleanTitle.length > 100 || note.value.length > 500) {
    uni.showToast({ title: '标题需为1至100字，备注不超过500字', icon: 'none' }); return null
  }
  if (!date.value || !time.value || date.value > '2099-12-31') {
    uni.showToast({ title: '请选择有效时间', icon: 'none' }); return null
  }
  const dueAt = `${date.value}T${time.value}:00`
  if (dueAt.slice(0, 16) <= beijingTime()) {
    uni.showToast({ title: '计划时间须晚于现在', icon: 'none' }); return null
  }
  const repeat = cleanRepeat(customRepeat.value)
  const error = repeatError(repeat)
  if (error) { uni.showToast({ title: error, icon: 'none' }); return null }
  return { ...repeat, title: cleanTitle, note: note.value.trim(), recurrence: 'CUSTOM',
    monthInterval: 1, dueAt, remind: remind.value }
}
async function save() {
  if (saving.value || loading.value || loadError.value) return
  const data = payload()
  if (!data) return
  const signature = `${editId.value || 'create'}:${JSON.stringify(data)}`
  saving.value = true
  try {
    const body = { ...data, idempotencyKey: stableKey(signature) }
    if (editId.value) await todoApi.edit(editId.value, body)
    else await todoApi.create(body)
    requestKeys.delete(signature)
    uni.showToast({ title: '已保存', icon: 'none' })
    returnAfterSave()
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '保存失败，请重试', icon: 'none' })
  } finally { saving.value = false }
}
</script>

<template>
  <view class="page profile-page todo-editor-page">
    <NativeNavigation variant="help" :title="editId ? '编辑待办' : '新增待办'" back-label="返回待办清单" @back="back" />
    <view v-if="loading" class="profile-loading">正在加载待办…</view>
    <view v-else-if="loadError" class="profile-error"><text>{{ loadError }}</text><button v-if="editId" class="text-button" @click="loadDetail">重新加载</button><button class="text-button" @click="returnToList">返回清单</button></view>
    <template v-else>
      <view class="profile-hero todo-editor-hero">
        <text class="profile-eyebrow">TODO LIST</text>
        <text class="profile-hero-title">{{ editId ? '调整这件事的计划' : '安排好下一件事' }}</text>
        <text class="profile-hero-copy">{{ editId ? '修改后，已完成记录会保留原来的内容。' : '设定时间与重复方式，按自己的节奏完成。' }}</text>
        <view class="profile-orbit profile-orbit-one" aria-hidden="true" /><view class="profile-orbit profile-orbit-two" aria-hidden="true" />
      </view>
      <view class="todo-editor-card">
        <view class="todo-editor-field"><text class="todo-editor-label">标题 <text class="todo-required">*</text></text><input v-model="title" aria-label="待办标题" maxlength="100" placeholder="要完成什么？" :disabled="saving" /></view>
        <view class="todo-editor-field"><text class="todo-editor-label">备注 <text class="todo-optional">选填</text></text><textarea v-model="note" aria-label="待办备注" maxlength="500" placeholder="写下补充信息" :disabled="saving" /></view>
        <view class="todo-editor-field"><text class="todo-editor-label">{{ editId ? '下次计划时间 · 北京时间' : '首次计划时间 · 北京时间' }} <text class="todo-required">*</text></text><view class="todo-editor-date-row"><button :disabled="saving" @click="pickerMode = 'date'">{{ date || '选择日期' }}</button><button :disabled="saving" @click="pickerMode = 'time'">{{ time || '选择时间' }}</button></view></view>
        <view class="todo-editor-field">
          <text class="todo-editor-label">重复 <text class="todo-required">*</text></text>
          <TodoRepeatFieldsInput v-model="customRepeat" :disabled="saving" :min-date="beijingTime().slice(0, 10)" />
        </view>
        <view class="todo-editor-remind"><view><text>到期提醒</text><text>通过通知中心已配置的 Bark / pushplus 发送</text></view><switch :checked="remind" color="#49ad9c" :disabled="saving" @change="chooseRemind" /></view>
      </view>
      <text class="todo-editor-footnote">到期仍未完成时发送提醒。删除未完成的重复待办会结束整条规则。</text>
      <view class="profile-actions todo-editor-actions"><button class="profile-password-action" :disabled="saving" @click="back">取消</button><button class="profile-save-action" :disabled="saving" @click="save">{{ saving ? '保存中…' : editId ? '保存修改' : '保存待办' }}</button></view>
    </template>
    <EntryDateTimePicker v-if="pickerMode" :mode="pickerMode" :value="pickerMode === 'date' ? date : time" :title="pickerMode === 'date' ? '选择待办日期' : '选择待办时间'" :min-date="beijingTime().slice(0, 10)" @close="pickerMode = ''" @select="selectPicker" />
    <view v-if="discardOpen" class="asset-create-backdrop" @click.self="discardOpen = false" @touchmove.stop.prevent>
      <view class="account-delete-modal" role="dialog" aria-modal="true" aria-label="放弃本次编辑">
        <view class="asset-create-handle" /><text class="account-delete-title">放弃本次编辑？</text>
        <text class="account-delete-copy">未保存的内容将丢失。</text>
        <view class="account-delete-actions"><button class="account-delete-cancel" @click="discardOpen = false">继续编辑</button><button class="account-delete-confirm" @click="discardOpen = false; returnToList()">放弃</button></view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.todo-editor-page { padding-bottom:calc(110px + env(safe-area-inset-bottom)); }
.todo-editor-hero { min-height:125px; }
.todo-editor-card { overflow:hidden; margin-top:18px; padding:0 16px; border:1px solid #edf1ef; border-radius:18px; background:#fff; box-shadow:0 7px 18px rgba(49,76,71,.05); }
.todo-editor-field { padding:16px 0; border-bottom:1px solid #f0f3f1; }
.todo-editor-label { display:block; margin-bottom:9px; color:#52655f; font-size:12px; font-weight:700; }
.todo-editor-field > input, .todo-editor-field > textarea { width:100%; min-height:44px; padding:10px 12px; border:1px solid #deebe7; border-radius:11px; color:#253b34; background:#f9fcfb; font-size:13px; box-sizing:border-box; }
.todo-editor-field textarea { height:78px; }
.todo-editor-date-row { display:flex; gap:9px; }
.todo-editor-date-row button { flex:1; min-width:0; padding:0 8px; display:flex; align-items:center; justify-content:center; min-height:44px; margin:0; border:1px solid #deebe7; border-radius:11px; color:#26776a; background:#f9fcfb; font-size:13px; line-height:1.2; }
.todo-required { color:#d9796b; }
.todo-optional { margin-left:5px; color:#8a9f96; font-weight:400; }
.todo-editor-remind { display:flex; align-items:center; justify-content:space-between; gap:8px; padding:17px 0; }
.todo-editor-remind > view { flex:1; min-width:0; display:flex; flex-direction:column; gap:5px; }
.todo-editor-remind > switch { flex:0 0 auto; }
.todo-editor-remind text:first-child { color:#52655f; font-size:12px; font-weight:700; }
.todo-editor-remind text:last-child { color:#8a9f96; font-size:10px; line-height:1.5; }
.todo-editor-footnote { display:block; margin:13px 4px 0; color:#91a49f; font-size:10px; line-height:1.6; }
.todo-editor-actions button, .todo-editor-page .account-delete-actions button, .todo-editor-page .text-button { display:flex; align-items:center; justify-content:center; margin:0; line-height:1.2; text-align:center; }
</style>
