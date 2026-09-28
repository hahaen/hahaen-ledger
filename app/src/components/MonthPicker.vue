<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
const props = defineProps<{ value: string }>()
const emit = defineEmits<{ select: [month: string]; close: [] }>()
const year = ref(Number(props.value.slice(0, 4)))
const month = ref(Number(props.value.slice(5, 7)))
const years = Array.from({ length: 31 }, (_, index) => new Date().getFullYear() - 15 + index)
const months = Array.from({ length: 12 }, (_, index) => index + 1)
const yearScrollTop = ref(0)
const monthScrollTop = ref(0)
const optionHeight = 48
const visibleOptions = 5
const centerOffset = ((visibleOptions - 1) / 2) * optionHeight
function centeredScrollTop(index: number) { return Math.max(0, index * optionHeight - centerOffset) }
function selectYear(value: number) { year.value = value; yearScrollTop.value = centeredScrollTop(years.indexOf(value)) }
function selectMonth(value: number) { month.value = value; monthScrollTop.value = centeredScrollTop(months.indexOf(value)) }
function syncScrollPosition() {
  yearScrollTop.value = centeredScrollTop(years.indexOf(year.value))
  monthScrollTop.value = centeredScrollTop(months.indexOf(month.value))
}
onMounted(async () => {
  await nextTick()
  syncScrollPosition()
  setTimeout(syncScrollPosition, 80)
})
</script>
<template>
  <view class="month-picker-backdrop" @click.self="emit('close')" @touchmove.stop.prevent>
    <view class="month-picker-modal" role="dialog" aria-modal="true" aria-label="日期">
      <view class="month-picker-handle" />
      <text class="month-picker-title">日期</text>
      <view class="date-picker-columns">
        <view class="date-picker-column"><text class="date-picker-label">年份</text><scroll-view scroll-y :scroll-top="yearScrollTop" class="date-picker-scroll"><button v-for="value in years" :key="value" :class="['date-picker-option', { selected: year === value }]" @click="selectYear(value)">{{ value }}年</button></scroll-view></view>
        <view class="date-picker-column"><text class="date-picker-label">月份</text><scroll-view scroll-y :scroll-top="monthScrollTop" class="date-picker-scroll"><button v-for="value in months" :key="value" :class="['date-picker-option', { selected: month === value }]" @click="selectMonth(value)">{{ value }}月</button></scroll-view></view>
      </view>
      <view class="month-picker-actions"><button class="month-picker-cancel" @click="emit('close')">取消</button><button class="month-picker-confirm" @click="emit('select', `${year}-${String(month).padStart(2, '0')}`)">确定</button></view>
    </view>
  </view>
</template>
