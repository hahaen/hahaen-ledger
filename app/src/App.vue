<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { useLedger } from './stores/ledger'
import { setAuthExpiredHandler } from './utils/api'
import { currentH5Path, H5_LOGIN_PATH, installH5AuthGuard, isH5AuthPath, shouldRedirectAuthenticatedH5UserToHome } from './utils/h5AuthGuard'
const ledger = useLedger()
const initialized = ref(false)

// #ifdef H5
const h5AuthGuard = installH5AuthGuard({
  hasSession: () => Boolean(ledger.state.token),
  redirectToLogin: () => uni.reLaunch({ url: H5_LOGIN_PATH }),
})
// #endif

setAuthExpiredHandler(() => {
  ledger.clearSession()
  // #ifdef H5
  if (initialized.value) h5AuthGuard.enforce()
  // #endif
})

onLaunch(async () => {
  await ledger.restore()
  // #ifdef H5
  const currentPath = currentH5Path(window.location)
  const isAuthPage = isH5AuthPath(currentPath)
  if (ledger.state.token) {
    // 业务数据由当前页面加载，避免首页 onShow 与应用启动阶段重复请求。
    if (shouldRedirectAuthenticatedH5UserToHome(currentPath)) uni.reLaunch({ url: '/pages/index/index' })
  } else if (!isAuthPage) {
    h5AuthGuard.enforce()
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
