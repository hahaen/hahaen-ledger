<script setup lang="ts">
import { computed, ref } from 'vue'
import type { TodoRepeatFields } from '../utils/api'
import { repeatModes, repeatUnits, splitSelection, toggleSelection } from '../utils/todoRepeat'
const props = defineProps<{ modelValue: TodoRepeatFields; disabled: boolean; minDate: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: TodoRepeatFields] }>()
const calendarMonth = ref(props.minDate.slice(0, 7))
const annualMonth = ref(Number(props.minDate.slice(5, 7)))
const fixed = computed(() => props.modelValue.repeatMode === 'FIXED_DATES')
const weekdays = ['一', '二', '三', '四', '五', '六', '日']
function patch(value: Partial<TodoRepeatFields>) { if (!props.disabled) emit('update:modelValue', { ...props.modelValue, ...value }) }
function pickInterval(event: Event) {
  const input = event as Event & { detail?: { value?: string }; target: HTMLInputElement | null }
  patch({ repeatInterval: Number(input.detail?.value ?? input.target?.value ?? '') })
}
function pickLastDay(event: Event) { patch({ lastDay: Boolean((event as Event & { detail?: { value?: boolean } }).detail?.value) }) }
function moveAnnualMonth(delta: number) { if (!props.disabled) annualMonth.value = (annualMonth.value - 1 + delta + 12) % 12 + 1 }
const calendar = computed(() => {
  const [year, month] = fixed.value ? calendarMonth.value.split('-').map(Number) : [2000, annualMonth.value]
  const count = new Date(Date.UTC(year, month, 0)).getUTCDate()
  const offset = (new Date(Date.UTC(year, month - 1, 1)).getUTCDay() + 6) % 7
  return { year, month, cells: [...Array.from({ length: offset }, () => 0), ...Array.from({ length: count }, (_, i) => i + 1)] }
})
function dateKey(day: number) {
  const monthDay = `${String(calendar.value.month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  return fixed.value ? `${calendar.value.year}-${monthDay}` : monthDay
}
function selected(day: number) { return splitSelection(fixed.value ? props.modelValue.fixedDates : props.modelValue.yearDays).includes(dateKey(day)) }
function unavailable(day: number) { return fixed.value && (dateKey(day) < props.minDate || dateKey(day) > '2099-12-31') }
function toggleDate(day: number) {
  if (props.disabled || unavailable(day)) return
  const field = fixed.value ? 'fixedDates' : 'yearDays'
  const values = splitSelection(props.modelValue[field])
  if (fixed.value && values.length >= 100 && !values.includes(dateKey(day))) { uni.showToast({ title: '最多选择100个固定日期', icon: 'none' }); return }
  patch({ [field]: toggleSelection(props.modelValue[field], dateKey(day)) })
}
function moveMonth(delta: number) {
  if (props.disabled) return
  const [year, month] = calendarMonth.value.split('-').map(Number)
  const next = new Date(Date.UTC(year, month - 1 + delta, 1)).toISOString().slice(0, 7)
  if (next >= props.minDate.slice(0, 7) && next <= '2099-12') calendarMonth.value = next
}
</script>
<template>
  <view class="repeat-fields">
    <view class="repeat-modes"><button v-for="mode in repeatModes" :key="mode.value" :class="{ selected: modelValue.repeatMode === mode.value }" :disabled="disabled" :aria-pressed="modelValue.repeatMode === mode.value" @click="patch({ repeatMode: mode.value })">{{ mode.label }}</button></view>
    <text class="repeat-tip">{{ fixed ? '选择多个公历日期，到所选日期按计划时间执行。' : modelValue.repeatMode === 'AFTER_COMPLETION' ? '完成后才创建下一次，以实际完成时间加上所选间隔。' : '以首次计划日期为基准，按规则生成待办；未完成不影响下一次。' }}</text>
    <view v-if="!fixed" class="repeat-interval"><text>每</text><input :value="modelValue.repeatInterval" type="number" maxlength="3" aria-label="重复间隔，1至365" :disabled="disabled" @input="pickInterval" /><text>{{ repeatUnits.find(unit => unit.value === modelValue.repeatUnit)?.label }}</text></view>
    <view v-if="!fixed" class="repeat-modes repeat-units"><button v-for="unit in repeatUnits" :key="unit.value" :disabled="disabled" :class="{ selected: modelValue.repeatUnit === unit.value }" :aria-pressed="modelValue.repeatUnit === unit.value" @click="patch({ repeatUnit: unit.value })">{{ unit.label }}</button></view>
    <template v-if="modelValue.repeatMode === 'TIME'">
      <view v-if="modelValue.repeatUnit === 'WEEK'" class="repeat-grid"><button v-for="(day, index) in weekdays" :key="day" :disabled="disabled" :class="{ selected: splitSelection(modelValue.weekDays).includes(String(index + 1)) }" :aria-pressed="splitSelection(modelValue.weekDays).includes(String(index + 1))" @click="patch({ weekDays: toggleSelection(modelValue.weekDays, String(index + 1), true) })">{{ day }}</button></view>
      <template v-if="modelValue.repeatUnit === 'MONTH'"><view class="repeat-grid"><button v-for="day in 31" :key="day" :disabled="disabled" :class="{ selected: splitSelection(modelValue.monthDays).includes(String(day)) }" :aria-pressed="splitSelection(modelValue.monthDays).includes(String(day))" @click="patch({ monthDays: toggleSelection(modelValue.monthDays, String(day), true) })">{{ day }}</button></view><view class="repeat-last"><text>每月最后一天</text><switch :checked="modelValue.lastDay" :disabled="disabled" color="#49ad9c" @change="pickLastDay" /></view><text class="repeat-tip">该月没有所选日期时取月末，同一天只生成一次。</text></template>
    </template>
    <template v-if="fixed || modelValue.repeatMode === 'TIME' && modelValue.repeatUnit === 'YEAR'">
      <view v-if="fixed" class="repeat-calendar-nav"><button aria-label="上个月" :disabled="disabled || calendarMonth <= minDate.slice(0, 7)" @click="moveMonth(-1)">‹</button><text>{{ calendarMonth }}</text><button aria-label="下个月" :disabled="disabled || calendarMonth >= '2099-12'" @click="moveMonth(1)">›</button></view>
      <view v-else class="repeat-calendar-nav"><button aria-label="上一月" :disabled="disabled" @click="moveAnnualMonth(-1)">‹</button><text>{{ annualMonth }}月</text><button aria-label="下一月" :disabled="disabled" @click="moveAnnualMonth(1)">›</button></view>
      <view class="repeat-week"><text v-for="day in weekdays" :key="day">{{ day }}</text></view><view class="repeat-grid"><view v-for="(day, index) in calendar.cells" :key="index"><button v-if="day" :disabled="disabled || unavailable(day)" :class="{ selected: selected(day) }" :aria-pressed="selected(day)" :aria-label="dateKey(day)" @click="toggleDate(day)">{{ day }}</button></view></view>
      <view class="repeat-dates"><button v-for="day in splitSelection(fixed ? modelValue.fixedDates : modelValue.yearDays)" :key="day" :disabled="disabled" :aria-label="`移除${day}`" @click="patch(fixed ? { fixedDates: toggleSelection(modelValue.fixedDates, day) } : { yearDays: toggleSelection(modelValue.yearDays, day) })">{{ day }} ×</button></view>
      <text class="repeat-tip">{{ fixed ? '首次计划时间为起点，早于起点的日期不执行；最多100个日期。' : '可跨月多选；2月29日在非闰年取2月末。' }}</text>
    </template>
  </view>
</template>
<style scoped>
.repeat-fields { margin-top:12px; }
.repeat-modes, .repeat-interval, .repeat-calendar-nav, .repeat-last { display:flex; align-items:center; gap:8px; }
.repeat-fields button { display:flex; align-items:center; justify-content:center; min-height:44px; margin:0; padding:0 4px; border:1px solid #deebe7; border-radius:10px; background:#f9fcfb; color:#52655f; font-size:13px; line-height:1.2; }
.repeat-fields button::after { border:0; }
.repeat-modes button { flex:1; min-width:0; }
.repeat-fields .selected { background:#e2f4ef; border-color:#49ad9c; color:#26776a; font-weight:700; }
.repeat-tip { display:block; margin:9px 0; color:#6e817a; font-size:12px; line-height:1.6; }
.repeat-interval { min-height:44px; margin:10px 0; color:#52655f; font-size:13px; }
.repeat-interval input { flex:0 0 68px; width:68px; min-width:0; height:36px; padding:0 8px; text-align:center; color:#253b34; font-size:14px; box-sizing:border-box; border:1px solid #deebe7; border-radius:10px; background:#f9fcfb; }
.repeat-units { margin-bottom:12px; }
.repeat-grid, .repeat-week { display:grid; grid-template-columns:repeat(7,minmax(0,1fr)); gap:4px; }
.repeat-grid > view { min-width:0; }
.repeat-grid button { width:100%; min-width:0; }
.repeat-week { margin:12px 0 6px; text-align:center; color:#6e817a; font-size:12px; }
.repeat-last > switch { flex:0 0 auto; }
.repeat-last { justify-content:space-between; margin-top:12px; color:#52655f; font-size:13px; }
.repeat-calendar-nav { justify-content:space-between; margin-top:12px; color:#52655f; }
.repeat-calendar-nav button { width:44px; font-size:24px; }
.repeat-dates { display:flex; flex-wrap:wrap; gap:6px; margin-top:10px; }
.repeat-dates button { max-width:100%; padding:0 8px; word-break:break-all; }
.repeat-fields button[disabled] { opacity:.45; }
</style>
