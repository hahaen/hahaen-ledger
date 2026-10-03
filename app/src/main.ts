import { createSSRApp } from 'vue'
import App from './App.vue'
import './styles.scss'
// #ifdef MP-WEIXIN
import { getWechatUpdateGuard } from './utils/wechatUpdate'
// #endif

export function createApp() {
  const app = createSSRApp(App)
  // #ifdef MP-WEIXIN
  // 页面显示（含深链接、原生导航）时恢复全局更新拦截。
  app.mixin({ onShow() { getWechatUpdateGuard().enforce() } })
  // #endif
  return { app }
}
