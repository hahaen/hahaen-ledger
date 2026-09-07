<script setup lang="ts">
import { onMounted } from 'vue'
defineProps<{ active: 'home' | 'calendar' | 'assets' | 'mine' }>()
const items = [
  { key: 'home', label: '首页', icon: '⌂', path: 'index/index' },
  { key: 'calendar', label: '日历', icon: '◷', path: 'calendar/calendar' },
  { key: 'assets', label: '资产', icon: '▣', path: 'assets/assets' },
  { key: 'mine', label: '我的', icon: '◎', path: 'mine/mine' },
]
// 页面已提供自定义导航，隐藏平台导航以避免重叠。
onMounted(() => uni.hideTabBar({ animation: false }))
function go(url: string) { uni.switchTab({ url }) }
</script>
<template>
  <view class="bottom-nav" aria-label="主导航"><button v-for="item in items" :key="item.key" :class="['nav-item', { active: active === item.key }]" :aria-label="item.label" @click="go(`/pages/${item.path}`)"><text class="nav-icon">{{ item.icon }}</text><text>{{ item.label }}</text></button></view>
</template>
