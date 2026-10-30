<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{ red?: string | null; blue?: string | null }>()
const redNumbers = computed(() => props.red?.split(',').filter(Boolean) || [])
const blueNumbers = computed(() => props.blue?.split(',').filter(Boolean) || [])
</script>
<template>
  <view class="lottery-numbers" aria-label="彩票号码">
    <text v-for="n in redNumbers" :key="`r${n}`" class="lottery-ball lottery-ball-red">{{ n }}</text>
    <text v-if="blueNumbers.length" class="lottery-number-separator" aria-hidden="true">＋</text>
    <text v-for="n in blueNumbers" :key="`b${n}`" class="lottery-ball lottery-ball-blue">{{ n }}</text>
  </view>
</template>
