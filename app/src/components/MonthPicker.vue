<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import CenterModal from './CenterModal.vue'
const props = defineProps<{ value: string }>()
const emit = defineEmits<{ select: [month: string]; close: [] }>()
const year = ref(Number(props.value.slice(0, 4)))
const month = ref(Number(props.value.slice(5, 7)))
const years = Array.from({ length: 31 }, (_, index) => new Date().getFullYear() - 15 + index)
const months = Array.from({ length: 12 }, (_, index) => index + 1)
const yearTarget = ref('')
const monthTarget = ref('')
onMounted(async () => { await nextTick(); yearTarget.value = 'year-' + year.value; monthTarget.value = 'month-' + month.value })
</script>
<template>
  <CenterModal title="选择月份" @close="emit('close')">
    <view class="date-picker-columns"><view><text class="date-picker-label">年份</text><scroll-view scroll-y :scroll-into-view="yearTarget" class="date-picker-scroll"><button v-for="value in years" :id="'year-' + value" :key="value" :class="['date-picker-option', { selected: year === value }]" @click="year = value">{{ value }}年</button></scroll-view></view><view><text class="date-picker-label">月份</text><scroll-view scroll-y :scroll-into-view="monthTarget" class="date-picker-scroll"><button v-for="value in months" :id="'month-' + value" :key="value" :class="['date-picker-option', { selected: month === value }]" @click="month = value">{{ value }}月</button></scroll-view></view></view>
    <template #actions><view class="sheet-actions"><button class="secondary-btn" @click="emit('close')">取消</button><button class="primary-btn" @click="emit('select', `${year}-${String(month).padStart(2, '0')}`)">确定</button></view></template>
  </CenterModal>
</template>
