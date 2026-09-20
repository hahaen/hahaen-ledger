<script setup lang="ts">
import NativeNavigation from '../../components/NativeNavigation.vue'
import { handleMpTouchStart } from '../../utils/tapFeedback'
import { registerWechatShare } from '../../utils/wechatShare'

registerWechatShare()

function enter() { uni.setStorageSync('first-use-complete', true); uni.reLaunch({ url: '/pages/index/index' }) }
</script>
<template>
  <view class="page first-use" @touchstart.capture="handleMpTouchStart">
    <NativeNavigation variant="welcome" :page-top-extra="60" />
    <text class="eyebrow">A LITTLE BOOKKEEPER</text>
    <view class="first-illustration"><view class="leaf" /></view>
    <view class="welcome-title">记录每一笔，<view class="expense">让生活更清晰</view></view>
    <view class="welcome-copy">用 3 秒记下一笔，<view>用 10 秒看懂这个月。</view></view>
    <button data-tap-feedback="true" class="primary-btn" @click="enter">开始记账</button><button data-tap-feedback="true" class="skip-link" @click="enter">稍后设置账户</button>
  </view>
</template>
