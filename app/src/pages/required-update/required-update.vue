<script lang="ts">
// #ifdef MP-WEIXIN
import { createWechatShareMessage } from '../../utils/wechatShare'
// #endif
export default {
  // #ifdef MP-WEIXIN
  onShareAppMessage: createWechatShareMessage,
  // #endif
}
</script>

<script setup lang="ts">
import NativeNavigation from '../../components/NativeNavigation.vue'
// #ifdef H5
import { onShow } from '@dcloudio/uni-app'
// #endif
// #ifdef MP-WEIXIN
import { getWechatUpdateGuard } from '../../utils/wechatUpdate'
const update = getWechatUpdateGuard()
const state = update.state
const message = update.message
// #endif
// #ifdef H5
onShow(() => uni.reLaunch({ url: '/pages/index/index' }))
// #endif
</script>

<template>
  <view class="page update-page">
    <NativeNavigation variant="welcome" />
    <!-- #ifdef MP-WEIXIN -->
    <view class="update-content" role="alert" aria-live="polite">
      <text class="update-title">{{ state === 'downloading' ? '正在更新' : '需要更新后使用' }}</text>
      <text class="update-message">{{ message }}</text>
      <button v-if="state === 'ready'" class="primary-btn" @click="update.apply">重启更新</button>
      <button v-else-if="state === 'failed' || state === 'unsupported'" class="primary-btn" @click="update.prompt">查看更新提示</button>
      <text v-else class="update-status">{{ state === 'restarting' ? '正在重启…' : '请稍候…' }}</text>
    </view>
    <!-- #endif -->
  </view>
</template>

<style scoped>
.update-page { min-height:100vh; box-sizing:border-box; padding-bottom:calc(32px + env(safe-area-inset-bottom)); }
.update-content { margin:auto 0; padding:32px 8px; }
.update-title { display:block; font-size:24px; font-weight:700; line-height:1.4; }
.update-message { display:block; margin:16px 0 28px; font-size:16px; line-height:1.7; color:#455651; }
.update-status { display:block; font-size:14px; color:#455651; }
.primary-btn { min-height:48px; background:#164f48; }
</style>
