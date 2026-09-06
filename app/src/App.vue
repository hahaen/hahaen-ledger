<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { useLedger } from './stores/ledger'
const ledger = useLedger()
const initialized = ref(false)
onLaunch(async () => {
  await ledger.restore()
  // #ifdef H5
  const currentPath = window.location.hash.replace(/^#/, '').split('?')[0] || '/'
  const isAuthPage = currentPath === '/pages/auth/login/login' || currentPath === '/pages/auth/register/register'
  if (ledger.state.token) {
    try {
      await ledger.refresh()
      uni.reLaunch({ url: '/pages/index/index' })
    } catch {
      uni.removeStorageSync('auth-token')
      if (!isAuthPage) uni.reLaunch({ url: '/pages/auth/login/login' })
    }
  } else if (!isAuthPage) {
    uni.reLaunch({ url: '/pages/auth/login/login' })
  }
  // #endif
  // #ifdef MP-WEIXIN
  try { if (ledger.state.token) await ledger.refresh(); else await ledger.login() } catch { /* 页面仍可进入，后续由页面重试 */ }
  if (!uni.getStorageSync('first-use-complete')) uni.reLaunch({ url: '/pages/first-use/first-use' })
  // #endif
  initialized.value = true
})
</script>
<template>
  <view v-if="!initialized" class="app-boot" aria-label="正在加载哈记账" />
  <slot v-else />
</template>
