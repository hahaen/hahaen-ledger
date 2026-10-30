import { createSSRApp } from 'vue'
import App from './App.vue'
import './styles.scss'
// #ifdef H5
import { installH5Update } from './utils/h5UpdateBrowser'
// #endif
// #ifdef MP-WEIXIN
import { getWechatUpdateGuard } from './utils/wechatUpdate'
// #endif

export function createApp() {
  const app = createSSRApp(App)
  // #ifdef H5
  if (__H5_BUILD_VERSION__) installH5Update(__H5_BUILD_VERSION__)
  // #endif
  // #ifdef MP-WEIXIN
  // 页面显示（含深链接、原生导航）时恢复全局更新拦截。
  app.mixin({ onShow() { getWechatUpdateGuard().enforce() } })
  // #endif
  return { app }
}
