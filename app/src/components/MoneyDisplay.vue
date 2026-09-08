<script setup lang="ts">
import { computed } from 'vue'
import { formatYuan, type MoneyCents } from '../utils/money'

const props = withDefaults(defineProps<{ value: MoneyCents; prefix?: string }>(), {
  prefix: '',
})
const displayValue = computed(() => formatYuan(props.value))
</script>

<template>
  <view :class="['money-display', { 'money-long': displayValue.length > 10 }]">
    <text v-if="prefix" class="money-prefix">{{ prefix }}</text>
    <text class="money-value">{{ displayValue }}</text>
  </view>
</template>
