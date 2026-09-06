<script setup lang="ts">
import BottomNav from '../../components/BottomNav.vue'
function authorize() { uni.showToast({ title: '头像授权入口已保留', icon: 'none' }) }
function help() { uni.navigateTo({ url: '/pages/help/help' }) }
</script>
<template><view class="page"><view class="brand"><image src="/static/brand.png"/><view><text class="brand-name">我的</text><text class="brand-sub">轻松记账，安心生活</text></view></view><view class="card" style="text-align:center"><image src="/static/brand.png" style="width:72px;height:72px;border-radius:24px"/><text class="section-title" style="display:block;margin:10px 0 4px">账本主人</text><text class="asset-sub">头像未授权 · 可继续使用</text><button class="secondary-btn" style="margin-top:14px" @click="authorize">去授权</button></view><view class="card"><view class="account-row"><text>累计记账天数</text><text class="asset-number" style="font-size:24px;color:#278879;margin:0">0 日</text></view></view><button class="card account-row" style="width:100%;text-align:left" @click="help"><text>关于与帮助</text><text class="arrow">›</text></button><BottomNav active="mine"/></view></template>
