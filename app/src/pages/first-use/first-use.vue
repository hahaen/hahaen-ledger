<script setup lang="ts">
function enter() { uni.setStorageSync('first-use-complete', true); uni.reLaunch({ url: '/pages/index/index' }) }
</script>
<template><view class="page first-use"><view class="brand"><image src="/static/brand.png"/><view><text class="brand-name">哈记账</text><text class="brand-sub">A LITTLE BOOKKEEPER</text></view></view><view class="first-illustration">🍃</view><view class="card" style="text-align:center"><text class="asset-number" style="font-size:28px">记录每一笔，让生活更清晰</text><text class="asset-sub" style="display:block;line-height:1.8">用 3 秒记下一笔，用 10 秒看懂这个月</text></view><button class="primary-btn" @click="enter">开始记账</button><button class="secondary-btn" style="margin-top:12px" @click="enter">稍后设置账户</button></view></template>
