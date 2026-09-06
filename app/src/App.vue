<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { useLedger } from './stores/ledger'
import { setAuthExpiredHandler } from './utils/api'
const ledger = useLedger()
const initialized = ref(false)

setAuthExpiredHandler(() => {
  ledger.clearSession()
  // #ifdef H5
  const path = currentH5Path()
  if (initialized.value && !isAuthPath(path)) uni.reLaunch({ url: '/pages/auth/login/login' })
  // #endif
})

// #ifdef H5
function currentH5Path() {
  return window.location.hash.replace(/^#/, '').split('?')[0] || '/'
}

function isAuthPath(path: string) {
  return path === '/pages/auth/login/login' || path === '/pages/auth/register/register'
}
// #endif

onLaunch(async () => {
  await ledger.restore()
  // #ifdef H5
  const currentPath = currentH5Path()
  const isAuthPage = isAuthPath(currentPath)
  if (ledger.state.token) {
    try {
      await ledger.refresh()
      // 只有根路径需要补到首页；业务页、详情页和认证页保持浏览器当前路由。
      if (currentPath === '/') uni.reLaunch({ url: '/pages/index/index' })
    } catch {
      ledger.clearSession()
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
