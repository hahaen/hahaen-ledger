<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'

const props = defineProps<{ mode: 'date' | 'time'; value: string }>()
const emit = defineEmits<{ close: []; select: [value: string] }>()
const dateParts = props.value.split('-').map(Number)
const timeParts = props.value.split(':').map(Number)
const nowYear = new Date().getFullYear()
const years = Array.from({ length: 31 }, (_, index) => nowYear - 15 + index)
const months = Array.from({ length: 12 }, (_, index) => index + 1)
const hours = Array.from({ length: 24 }, (_, index) => index)
const minutes = Array.from({ length: 60 }, (_, index) => index)
const year = ref(years.includes(dateParts[0]) ? dateParts[0] : nowYear)
const month = ref(dateParts[1] >= 1 && dateParts[1] <= 12 ? dateParts[1] : 1)
const day = ref(dateParts[2] || 1)
const hour = ref(timeParts[0] >= 0 && timeParts[0] <= 23 ? timeParts[0] : 0)
const minute = ref(timeParts[1] >= 0 && timeParts[1] <= 59 ? timeParts[1] : 0)
const yearScrollTop = ref(0)
const monthScrollTop = ref(0)
const dayScrollTop = ref(0)
const hourScrollTop = ref(0)
const minuteScrollTop = ref(0)
const optionHeight = 48
const centerOffset = optionHeight * 2
const days = computed(() => Array.from({ length: new Date(year.value, month.value, 0).getDate() }, (_, index) => index + 1))
function centeredScrollTop(index: number) { return Math.max(0, index * optionHeight - centerOffset) }
function normalizeDay() { if (day.value > days.value.length) day.value = days.value.length }
function selectYear(value: number) { year.value = value; normalizeDay(); yearScrollTop.value = centeredScrollTop(years.indexOf(value)) }
function selectMonth(value: number) { month.value = value; normalizeDay(); monthScrollTop.value = centeredScrollTop(months.indexOf(value)) }
function selectDay(value: number) { day.value = value; dayScrollTop.value = centeredScrollTop(days.value.indexOf(value)) }
function selectHour(value: number) { hour.value = value; hourScrollTop.value = centeredScrollTop(hours.indexOf(value)) }
function selectMinute(value: number) { minute.value = value; minuteScrollTop.value = centeredScrollTop(minutes.indexOf(value)) }
function syncScrollPosition() {
  normalizeDay()
  yearScrollTop.value = centeredScrollTop(years.indexOf(year.value))
  monthScrollTop.value = centeredScrollTop(months.indexOf(month.value))
  dayScrollTop.value = centeredScrollTop(days.value.indexOf(day.value))
  hourScrollTop.value = centeredScrollTop(hours.indexOf(hour.value))
  minuteScrollTop.value = centeredScrollTop(minutes.indexOf(minute.value))
}
function confirm() {
  if (props.mode === 'date') emit('select', `${year.value}-${String(month.value).padStart(2, '0')}-${String(day.value).padStart(2, '0')}`)
  else emit('select', `${String(hour.value).padStart(2, '0')}:${String(minute.value).padStart(2, '0')}`)
}
onMounted(async () => {
  await nextTick()
  syncScrollPosition()
  setTimeout(syncScrollPosition, 80)
})
</script>

<template>
  <view class="entry-value-picker-backdrop" @click.self="emit('close')" @touchmove.stop.prevent>
    <view class="entry-value-picker-modal" role="dialog" aria-modal="true" :aria-label="mode === 'date' ? '选择记账日期' : '选择记账时间'">
      <view class="entry-value-picker-handle" />
      <text class="entry-value-picker-title">{{ mode === 'date' ? '选择记账日期' : '选择记账时间' }}</text>
      <view :class="['entry-value-picker-columns', { 'two-columns': mode === 'time' }]">
        <template v-if="mode === 'date'">
          <view class="entry-value-picker-column"><text>年份</text><scroll-view scroll-y :scroll-top="yearScrollTop" class="entry-value-picker-scroll"><button v-for="value in years" :key="value" :class="['entry-value-picker-option', { selected: year === value }]" @click="selectYear(value)">{{ value }}年</button></scroll-view></view>
          <view class="entry-value-picker-column"><text>月份</text><scroll-view scroll-y :scroll-top="monthScrollTop" class="entry-value-picker-scroll"><button v-for="value in months" :key="value" :class="['entry-value-picker-option', { selected: month === value }]" @click="selectMonth(value)">{{ value }}月</button></scroll-view></view>
          <view class="entry-value-picker-column"><text>日期</text><scroll-view scroll-y :scroll-top="dayScrollTop" class="entry-value-picker-scroll"><button v-for="value in days" :key="value" :class="['entry-value-picker-option', { selected: day === value }]" @click="selectDay(value)">{{ value }}日</button></scroll-view></view>
        </template>
        <template v-else>
          <view class="entry-value-picker-column"><text>小时</text><scroll-view scroll-y :scroll-top="hourScrollTop" class="entry-value-picker-scroll"><button v-for="value in hours" :key="value" :class="['entry-value-picker-option', { selected: hour === value }]" @click="selectHour(value)">{{ String(value).padStart(2, '0') }}</button></scroll-view></view>
          <view class="entry-value-picker-column"><text>分钟</text><scroll-view scroll-y :scroll-top="minuteScrollTop" class="entry-value-picker-scroll"><button v-for="value in minutes" :key="value" :class="['entry-value-picker-option', { selected: minute === value }]" @click="selectMinute(value)">{{ String(value).padStart(2, '0') }}</button></scroll-view></view>
        </template>
      </view>
      <view class="entry-value-picker-actions"><button class="entry-value-picker-cancel" @click="emit('close')">取消</button><button class="entry-value-picker-confirm" @click="confirm">确定</button></view>
    </view>
  </view>
</template>
