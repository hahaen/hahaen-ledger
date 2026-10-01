<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'

const props = withDefaults(defineProps<{ mode: 'date' | 'time'; value: string; title?: string; minDate?: string; maxDate?: string }>(), {
  title: '', minDate: '', maxDate: '',
})
const emit = defineEmits<{ close: []; select: [value: string] }>()
const today = new Date(Date.now() + 8 * 60 * 60 * 1000).toISOString().slice(0, 10)
const dateParts = (props.value || today).split('-').map(Number)
const timeParts = props.value.split(':').map(Number)
const nowYear = Number(today.slice(0, 4))
const minDate = props.minDate || '2000-01-01'
const maxDate = props.maxDate || '2099-12-31'
const years = Array.from({ length: 100 }, (_, index) => 2000 + index)
const hours = Array.from({ length: 24 }, (_, index) => index)
const minutes = Array.from({ length: 60 }, (_, index) => index)
const year = ref(years.includes(dateParts[0]) ? dateParts[0] : Math.min(2099, Math.max(2000, nowYear)))
const month = ref(dateParts[1] >= 1 && dateParts[1] <= 12 ? dateParts[1] : Number(today.slice(5, 7)))
const day = ref(dateParts[2] >= 1 && dateParts[2] <= 31 ? dateParts[2] : Number(today.slice(8, 10)))
const hour = ref(timeParts[0] >= 0 && timeParts[0] <= 23 ? timeParts[0] : 0)
const minute = ref(timeParts[1] >= 0 && timeParts[1] <= 59 ? timeParts[1] : 0)
const yearScrollTop = ref(0)
const monthScrollTop = ref(0)
const dayScrollTop = ref(0)
const hourScrollTop = ref(0)
const minuteScrollTop = ref(0)
const validationError = ref('')
const optionHeight = 48
const centerOffset = optionHeight * 2
const months = computed(() => Array.from({ length: 12 }, (_, index) => index + 1))
const days = computed(() => Array.from({ length: new Date(year.value, month.value, 0).getDate() }, (_, index) => index + 1))
function centeredScrollTop(index: number) { return Math.max(0, index * optionHeight) }
function centeredYearScrollTop(index: number) { return index * optionHeight }
function normalizeDay() {
  if (day.value > days.value.length) day.value = days.value.length
}
function selectYear(value: number) { validationError.value = ''; year.value = value; normalizeDay(); yearScrollTop.value = centeredYearScrollTop(years.indexOf(value)); dayScrollTop.value = centeredScrollTop(days.value.indexOf(day.value)) }
function selectMonth(value: number) { validationError.value = ''; month.value = value; normalizeDay(); monthScrollTop.value = centeredScrollTop(months.value.indexOf(value)); dayScrollTop.value = centeredScrollTop(days.value.indexOf(day.value)) }
function selectDay(value: number) { validationError.value = ''; day.value = value; dayScrollTop.value = centeredScrollTop(days.value.indexOf(value)) }
function selectHour(value: number) { hour.value = value; hourScrollTop.value = centeredScrollTop(hours.indexOf(value)) }
function selectMinute(value: number) { minute.value = value; minuteScrollTop.value = centeredScrollTop(minutes.indexOf(value)) }
function centeredValue(values: number[], event: { detail: { scrollTop: number } }, leadingSpace = 0) {
  const index = Math.max(0, Math.min(values.length - 1, Math.round((event.detail.scrollTop + centerOffset - leadingSpace) / optionHeight)))
  return values[index]
}
function scrollYear(event: { detail: { scrollTop: number } }) {
  const value = centeredValue(years, event, centerOffset)
  if (value === undefined || value === year.value) return
  validationError.value = ''
  year.value = value
  normalizeDay()
  dayScrollTop.value = centeredScrollTop(days.value.indexOf(day.value))
}
function scrollMonth(event: { detail: { scrollTop: number } }) {
  const value = centeredValue(months.value, event, centerOffset)
  if (value === undefined || value === month.value) return
  validationError.value = ''
  month.value = value
  normalizeDay()
  dayScrollTop.value = centeredScrollTop(days.value.indexOf(day.value))
}
function scrollDay(event: { detail: { scrollTop: number } }) {
  const value = centeredValue(days.value, event, centerOffset)
  if (value !== undefined && value !== day.value) { validationError.value = ''; day.value = value }
}
function scrollHour(event: { detail: { scrollTop: number } }) {
  const value = centeredValue(hours, event, centerOffset)
  if (value !== undefined) hour.value = value
}
function scrollMinute(event: { detail: { scrollTop: number } }) {
  const value = centeredValue(minutes, event, centerOffset)
  if (value !== undefined) minute.value = value
}
function syncScrollPosition() {
  normalizeDay()
  yearScrollTop.value = centeredYearScrollTop(years.indexOf(year.value))
  monthScrollTop.value = centeredScrollTop(months.value.indexOf(month.value))
  dayScrollTop.value = centeredScrollTop(days.value.indexOf(day.value))
  hourScrollTop.value = centeredScrollTop(hours.indexOf(hour.value))
  minuteScrollTop.value = centeredScrollTop(minutes.indexOf(minute.value))
}
function confirm() {
  if (props.mode === 'date') {
    const value = `${year.value}-${String(month.value).padStart(2, '0')}-${String(day.value).padStart(2, '0')}`
    if (value < minDate || value > maxDate) { validationError.value = `日期须在 ${minDate} 至 ${maxDate} 之间`; return }
    emit('select', value)
  } else emit('select', `${String(hour.value).padStart(2, '0')}:${String(minute.value).padStart(2, '0')}`)
}
onMounted(async () => {
  await nextTick()
  syncScrollPosition()
  setTimeout(syncScrollPosition, 80)
})
// 仅 H5 阻止冒泡到页面禁滚监听；小程序不能在滚轮祖先使用 catchtouchmove。
function onPickerTouchMove(event: Event) {
  // #ifdef H5
  event.stopPropagation()
  // #endif
}
</script>

<template>
  <view class="entry-value-picker-backdrop">
    <view class="picker-touch-mask" @click.stop="emit('close')" @touchmove.stop.prevent />
    <view class="entry-value-picker-modal" @touchmove="onPickerTouchMove" role="dialog" aria-modal="true" :aria-label="title || (mode === 'date' ? '选择记账日期' : '选择记账时间')">
      <view class="entry-value-picker-handle" />
      <text class="entry-value-picker-title">{{ title || (mode === 'date' ? '选择记账日期' : '选择记账时间') }}</text>
      <view :class="['entry-value-picker-columns', { 'two-columns': mode === 'time' }]">
        <template v-if="mode === 'date'">
          <view class="entry-value-picker-column"><text>年份</text><scroll-view scroll-y :scroll-top="yearScrollTop" class="entry-value-picker-scroll" @scroll="scrollYear"><view class="entry-value-picker-year-spacer" /><button v-for="value in years" :key="value" :class="['entry-value-picker-option', { selected: year === value }]" @click.stop="selectYear(value)">{{ value }}年</button><view class="entry-value-picker-year-spacer" /></scroll-view></view>
          <view class="entry-value-picker-column"><text>月份</text><scroll-view scroll-y :scroll-top="monthScrollTop" class="entry-value-picker-scroll" @scroll="scrollMonth"><view class="entry-value-picker-year-spacer" /><button v-for="value in months" :key="value" :class="['entry-value-picker-option', { selected: month === value }]" @click.stop="selectMonth(value)">{{ value }}月</button><view class="entry-value-picker-year-spacer" /></scroll-view></view>
          <view class="entry-value-picker-column"><text>日期</text><scroll-view scroll-y :scroll-top="dayScrollTop" class="entry-value-picker-scroll" @scroll="scrollDay"><view class="entry-value-picker-year-spacer" /><button v-for="value in days" :key="value" :class="['entry-value-picker-option', { selected: day === value }]" @click.stop="selectDay(value)">{{ value }}日</button><view class="entry-value-picker-year-spacer" /></scroll-view></view>
        </template>
        <template v-else>
          <view class="entry-value-picker-column"><text>小时</text><scroll-view scroll-y :scroll-top="hourScrollTop" class="entry-value-picker-scroll" @scroll="scrollHour"><view class="entry-value-picker-year-spacer" /><button v-for="value in hours" :key="value" :class="['entry-value-picker-option', { selected: hour === value }]" @click.stop="selectHour(value)">{{ String(value).padStart(2, '0') }}</button><view class="entry-value-picker-year-spacer" /></scroll-view></view>
          <view class="entry-value-picker-column"><text>分钟</text><scroll-view scroll-y :scroll-top="minuteScrollTop" class="entry-value-picker-scroll" @scroll="scrollMinute"><view class="entry-value-picker-year-spacer" /><button v-for="value in minutes" :key="value" :class="['entry-value-picker-option', { selected: minute === value }]" @click.stop="selectMinute(value)">{{ String(value).padStart(2, '0') }}</button><view class="entry-value-picker-year-spacer" /></scroll-view></view>
        </template>
      </view>
      <text v-if="validationError" class="entry-value-picker-error" role="alert">{{ validationError }}</text>
      <view class="entry-value-picker-actions"><button class="entry-value-picker-cancel" @click.stop="emit('close')">取消</button><button class="entry-value-picker-confirm" @click.stop="confirm">确定</button></view>
    </view>
  </view>
</template>
