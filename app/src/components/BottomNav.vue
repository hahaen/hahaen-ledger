<script setup lang="ts">
import { onMounted } from 'vue'
const { active } = defineProps<{ active: 'home' | 'calendar' | 'assets' | 'mine' }>()
// 页面已提供自定义导航，隐藏平台导航以避免重叠。
onMounted(() => uni.hideTabBar({ animation: false }))
function go(url: string) { uni.switchTab({ url }) }
</script>
<template><view class="bottom-nav"><button :class="{active:active==='home'}" @click="go('/pages/index/index')">⌂<text>首页</text></button><button :class="{active:active==='calendar'}" @click="go('/pages/calendar/calendar')">◷<text>日历</text></button><button :class="{active:active==='assets'}" @click="go('/pages/assets/assets')">▣<text>资产</text></button><button :class="{active:active==='mine'}" @click="go('/pages/mine/mine')">◎<text>我的</text></button></view></template>
<style scoped>.bottom-nav{position:fixed;left:0;right:0;bottom:0;padding:8px 8px calc(8px + env(safe-area-inset-bottom));display:flex;background:#fff;border-top:1px solid #f0f1f1;z-index:10}.bottom-nav button{flex:1;background:transparent;color:#858b8b;font-size:21px;line-height:22px}.bottom-nav text{display:block;font-size:11px;margin-top:4px}.bottom-nav button.active{color:#278879;font-weight:600}</style>
