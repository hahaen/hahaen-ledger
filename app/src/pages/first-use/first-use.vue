<script setup lang="ts">
function enter() { uni.setStorageSync('first-use-complete', true); uni.reLaunch({ url: '/pages/index/index' }) }
</script>
<template>
  <view class="page first-use">
    <view class="welcome-brand"><image src="/static/brand.png" mode="aspectFill" /><text>哈记账</text></view>
    <text class="eyebrow">A LITTLE BOOKKEEPER</text>
    <view class="first-illustration"><view class="leaf" /></view>
    <view class="welcome-title">记录每一笔，<view class="expense">让生活更清晰</view></view>
    <view class="welcome-copy">用 3 秒记下一笔，<view>用 10 秒看懂这个月。</view></view>
    <button class="primary-btn" @click="enter">开始记账</button><button class="skip-link" @click="enter">稍后设置账户</button>
  </view>
</template>
