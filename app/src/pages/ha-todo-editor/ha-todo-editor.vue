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
import { computed, ref, watch } from 'vue'
import { onBackPress, onLoad, onUnload } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import EntryDateTimePicker from '../../components/EntryDateTimePicker.vue'
import TodoRepeatFieldsInput from '../../components/TodoRepeatFields.vue'
import { cleanRepeat, repeatError, repeatModes, splitSelection } from '../../utils/todoRepeat'
import { todoApi, type TodoPayload, type TodoRepeatFields, type TodoItem, type TodoPlan, type TodoPreview, type TodoRepeatMode } from '../../utils/api'
import { useLedger } from '../../stores/ledger'

const ledger = useLedger()
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
const mode = ref<'ONCE' | TodoRepeatMode>('ONCE')
const modes = [{ value: 'ONCE' as const, label: '不重复' }, ...repeatModes.map(value => ({ ...value, label: value.value === 'FIXED_DATES' ? '固定日期' : value.label }))]
const original = ref<TodoItem | null>(null)
const initialPlanSignature = ref('')
const preview = ref<TodoPreview | null>(null)
const previewError = ref('')
const previewLoading = ref(false)
let previewTimer: ReturnType<typeof setTimeout> | undefined
let previewSequence = 0
const initialValue = ref('')
const requestKeys = new Map<string, string>()
let allowBack = false

const beijingTime = (offsetMs = 0) => new Date(Date.now() + offsetMs + 8 * 60 * 60 * 1000).toISOString().slice(0, 16)
const snapshot = () => JSON.stringify([title.value, note.value, date.value, time.value, remind.value, mode.value, customRepeat.value])
const planSignature = () => JSON.stringify([mode.value, mode.value === 'FIXED_DATES' ? '' : date.value, time.value, mode.value === 'ONCE' ? null : cleanRepeat(customRepeat.value)])
const planChanged = computed(() => Boolean(original.value) && planSignature() !== initialPlanSignature.value)
const fixed = computed(() => mode.value === 'FIXED_DATES')
const dateLabel = computed(() => mode.value === 'TIME' ? '开始日期' : mode.value === 'AFTER_COMPLETION' ? (editId.value ? '本次计划日期' : '首次计划日期') : '计划日期')
const timeLabel = computed(() => mode.value === 'TIME' || fixed.value ? '执行时刻' : '计划时刻')
const niceDate = (value: string) => value.replace('T', ' ').slice(0, 16)
function selectMode(value: 'ONCE' | TodoRepeatMode) {
  if (saving.value) return
  mode.value = value
  if (value !== 'ONCE') customRepeat.value = { ...customRepeat.value, repeatMode: value }
}
function plan(): TodoPlan {
  const item = original.value
  if (item && !planChanged.value) {
    // 保留旧协议、原周期及秒/毫秒精度；UI展示精度不应改变实际计划。
    return { recurrence: item.recurrence, monthInterval: item.monthInterval || 1, dueAt: item.dueAt,
      ...(item.recurrence === 'CUSTOM' ? { repeatMode: item.repeatMode, repeatUnit: item.repeatUnit, repeatInterval: item.repeatInterval,
        weekDays: item.weekDays, monthDays: item.monthDays, lastDay: item.lastDay, yearDays: item.yearDays, fixedDates: item.fixedDates } : {}) }
  }
  const day = fixed.value ? [...splitSelection(customRepeat.value.fixedDates)].sort()[0] : date.value
  return { ...(mode.value === 'ONCE' ? {} : cleanRepeat(customRepeat.value)), recurrence: mode.value === 'ONCE' ? 'ONCE' : 'CUSTOM',
    monthInterval: 1, dueAt: `${day || ''}T${time.value}:00` }
}
function planError(): string {
  if (mode.value !== 'ONCE') { const error = repeatError(cleanRepeat(customRepeat.value)); if (error) return error }
  const value = plan()
  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?$/.test(value.dueAt) || value.dueAt.slice(0, 10) > '2099-12-31') return '请选择有效的日期和时间'
  if (original.value && !planChanged.value) return ''
  if (mode.value === 'TIME') return date.value < beijingTime().slice(0, 10) ? '开始日期不能早于今天' : ''
  return value.dueAt.slice(0, 16) <= beijingTime() ? '计划时间须晚于现在；固定日期请移除已过去的日期' : ''
}
async function refreshPreview() {
  clearTimeout(previewTimer)
  const sequence = ++previewSequence
  preview.value = null
  previewError.value = planError()
  previewLoading.value = false
  if (loading.value || loadError.value || previewError.value) return
  previewLoading.value = true
  try {
    const result = await todoApi.preview({ plan: plan(), ...(editId.value ? { occurrenceId: editId.value } : {}) })
    if (sequence === previewSequence) preview.value = result
  } catch (error) {
    if (sequence === previewSequence) previewError.value = error instanceof Error ? error.message : '计划预览失败，请重试'
  } finally { if (sequence === previewSequence) previewLoading.value = false }
}
watch(() => [planSignature(), loading.value], () => {
  ++previewSequence
  clearTimeout(previewTimer)
  preview.value = null
  previewError.value = ''
  previewLoading.value = !loading.value && !loadError.value
  if (previewLoading.value) previewTimer = setTimeout(() => { void refreshPreview() }, 250)
})
onUnload(() => { ++previewSequence; clearTimeout(previewTimer) })
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
    original.value = item
    mode.value = item.recurrence === 'ONCE' ? 'ONCE' : item.recurrence === 'CUSTOM' ? item.repeatMode || 'TIME' : 'TIME'
    const next = (mode.value === 'TIME' ? item.anchorAt || item.dueAt : item.dueAt).slice(0, 16)
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

    initialPlanSignature.value = planSignature()
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
  const error = planError()
  if (error) { uni.showToast({ title: error, icon: 'none' }); return null }
  return { ...plan(), title: cleanTitle, note: note.value.trim(), remind: remind.value }
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
    <NativeNavigation variant="help" :page-top-extra="20" :title="editId ? '编辑待办' : '新增待办'" back-label="返回待办清单" @back="back" />
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
        <view class="todo-editor-field">
          <text class="todo-editor-label">重复方式 <text class="todo-required">*</text></text>
          <view class="todo-plan-modes"><button v-for="option in modes" :key="option.value" :disabled="saving" :aria-pressed="mode === option.value" :class="{ selected: mode === option.value }" @click="selectMode(option.value)">{{ option.label }}</button></view>
          <text v-if="mode === 'ONCE'" class="todo-plan-tip">只生成一次待办，完成后不再重复。</text>
        </view>
        <view class="todo-editor-field">
          <text class="todo-editor-label">时间设置 · 北京时间 <text class="todo-required">*</text></text>
          <view class="todo-editor-date-row">
            <view v-if="!fixed"><text>{{ dateLabel }}</text><button :disabled="saving" :aria-label="dateLabel" @click="pickerMode = 'date'">{{ date || '选择日期' }}</button></view>
            <view><text>{{ timeLabel }}</text><button :disabled="saving" :aria-label="timeLabel" @click="pickerMode = 'time'">{{ time || '选择时间' }}</button></view>
          </view>
          <text v-if="mode === 'TIME'" class="todo-plan-tip">开始日期是重复周期的起点，实际首次待办以计划预览为准。</text>
          <text v-if="mode === 'AFTER_COMPLETION' && editId" class="todo-plan-tip">保留本次原计划；完成后，从实际完成时间计算下一次。</text>
        </view>
        <view v-if="mode !== 'ONCE'" class="todo-editor-field">
          <text class="todo-editor-label">{{ fixed ? '执行日期' : mode === 'AFTER_COMPLETION' ? '完成后间隔' : '重复规则' }} <text class="todo-required">*</text></text>
          <TodoRepeatFieldsInput v-model="customRepeat" :disabled="saving" :min-date="beijingTime().slice(0, 10)" />
        </view>
        <view class="todo-plan-preview" aria-live="polite">
          <text class="todo-editor-label">计划预览 · 北京时间</text>
          <text v-if="previewLoading">正在计算实际计划…</text>
          <template v-else-if="previewError"><text class="todo-plan-error">{{ previewError }}</text><button :disabled="saving" @click="refreshPreview">重新预览</button></template>
          <template v-else-if="preview">
            <text class="todo-plan-first">{{ editId && !preview.scheduleChanged ? '本次计划' : '首次待办' }}：{{ niceDate(preview.times[0]) }}</text>
            <text v-if="preview.times.length > 1">后续：{{ preview.times.slice(1).map(niceDate).join('、') }}</text>
            <text v-else>{{ preview.afterCompletion ? '后续时间在实际完成后，按所选间隔计算。' : mode === 'ONCE' ? '仅执行一次。' : '所选规则在2099年前没有更多待办。' }}</text>
          </template>
          <text v-if="editId" class="todo-plan-impact">{{ planChanged ? '将重新安排此任务的全部未完成待办，已完成记录保留。' : '保留原计划和重复周期，包括已逾期的待办；仅更新内容或提醒。' }}</text>
        </view>
        <view class="todo-editor-remind"><view><text>到期提醒</text><text>通过通知中心已配置的 Bark / pushplus 发送</text></view><switch :checked="remind" color="#49ad9c" :disabled="saving" @change="chooseRemind" /></view>
      </view>
      <text class="todo-editor-footnote">到期仍未完成时发送提醒。删除未完成的重复待办会结束整条规则。</text>
      <view class="profile-actions todo-editor-actions"><button class="profile-password-action" :disabled="saving" @click="back">取消</button><button class="profile-save-action" :disabled="saving" @click="save">{{ saving ? '保存中…' : editId ? '保存修改' : '保存待办' }}</button></view>
    </template>
    <EntryDateTimePicker v-if="pickerMode" :mode="pickerMode" :value="pickerMode === 'date' ? date : time" :title="pickerMode === 'date' ? '选择待办日期' : '选择待办时间'" :min-date="editId ? '2000-01-01' : beijingTime().slice(0, 10)" @close="pickerMode = ''" @select="selectPicker" />
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
.todo-editor-date-row > view { flex:1; min-width:0; display:flex; flex-direction:column; gap:7px; }
.todo-editor-date-row > view > text { color:#6e817a; font-size:12px; }
.todo-editor-date-row button { width:100%; min-width:0; padding:0 8px; display:flex; align-items:center; justify-content:center; min-height:44px; margin:0; border:1px solid #deebe7; border-radius:11px; color:#26776a; background:#f9fcfb; font-size:13px; line-height:1.2; }
.todo-plan-modes { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:8px; }
.todo-plan-modes button, .todo-plan-preview button { display:flex; align-items:center; justify-content:center; min-height:44px; margin:0; padding:8px; border:1px solid #deebe7; border-radius:10px; background:#f9fcfb; color:#52655f; font-size:13px; line-height:1.4; }
.todo-plan-modes button::after, .todo-plan-preview button::after { border:0; }
.todo-plan-modes .selected { background:#e2f4ef; border-color:#49ad9c; color:#26776a; font-weight:700; }
.todo-plan-tip { display:block; margin-top:9px; color:#6e817a; font-size:12px; line-height:1.6; }
.todo-plan-preview { display:flex; flex-direction:column; gap:8px; margin:16px 0 0; padding:14px 12px; border:1px solid #dcebe6; border-radius:12px; background:#f2faf7; color:#52655f; font-size:12px; line-height:1.7; overflow-wrap:anywhere; }
.todo-plan-preview .todo-editor-label { margin:0; }
.todo-plan-first { color:#26776a; font-weight:700; }
.todo-plan-preview .todo-plan-error { color:#a84636; }
.todo-plan-impact { padding-top:8px; border-top:1px solid #dcebe6; }
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
