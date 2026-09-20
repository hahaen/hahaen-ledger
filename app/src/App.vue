<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useLedger } from './stores/ledger'
import { setAuthExpiredHandler } from './utils/api'
import { currentH5Path, H5_LOGIN_PATH, installH5AuthGuard, isH5AuthPath, shouldRedirectAuthenticatedH5UserToHome } from './utils/h5AuthGuard'
import { installH5TapFeedback } from './utils/tapFeedback'
const ledger = useLedger()
const initialized = ref(false)
const mpLoginError = ref('')
let mpRecoveryPromise: Promise<void> | undefined
let removeH5TapFeedback = () => {}

onMounted(() => { removeH5TapFeedback = installH5TapFeedback() })
onBeforeUnmount(() => removeH5TapFeedback())

// #ifdef MP-WEIXIN
function redirectMpAuthPageToHome() {
  if (!ledger.state.token) return
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const route = currentPage?.route || ''
  if (route.startsWith('pages/auth/') || route === 'pages/first-use/first-use') {
    uni.reLaunch({ url: '/pages/index/index' })
  }
}

function recoverMpSession() {
  if (mpRecoveryPromise) return mpRecoveryPromise
  mpLoginError.value = ''
  const recoveryPromise = ledger.login()
    .then(() => { mpLoginError.value = '' })
    .catch(() => {
      mpLoginError.value = '微信登录失败，请检查网络后重试'
      throw new Error('微信自动重新登录失败')
    })
    .finally(() => { if (mpRecoveryPromise === recoveryPromise) mpRecoveryPromise = undefined })
  mpRecoveryPromise = recoveryPromise
  return recoveryPromise
}
// #endif

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
  // #ifdef MP-WEIXIN
  if (initialized.value) return recoverMpSession()
  // #endif
})

async function retryMpLogin() {
  mpLoginError.value = ''
  try {
    await ledger.login()
    redirectMpAuthPageToHome()
  } catch {
    mpLoginError.value = '微信登录失败，请检查网络后重试'
  }
}

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
  try {
    if (ledger.state.token) {
      try { await ledger.refresh() }
      catch { await ledger.login() }
    } else await ledger.login()
    redirectMpAuthPageToHome()
  } catch {
    mpLoginError.value = '微信登录失败，请检查网络后重试'
  }
  // #endif
  initialized.value = true
  // #ifdef MP-WEIXIN
  if (!mpLoginError.value) setTimeout(redirectMpAuthPageToHome, 0)
  // #endif
})
</script>
<template>
  <view v-if="!initialized" class="app-boot" aria-label="正在加载哈记账" />
  <view v-else-if="mpLoginError" class="page app-boot-error">
    <text class="profile-error">{{ mpLoginError }}</text>
    <button data-tap-feedback="true" class="primary-btn" @click="retryMpLogin">重试微信登录</button>
  </view>
  <slot v-else />
</template>
