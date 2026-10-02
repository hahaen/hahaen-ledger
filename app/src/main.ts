import { createSSRApp } from 'vue'
import App from './App.vue'
import './styles.scss'
// #ifdef MP-WEIXIN
import { wechatShareMixin } from './utils/wechatShare'
// #endif

export function createApp() {
  const app = createSSRApp(App)
  // #ifdef MP-WEIXIN
  app.mixin(wechatShareMixin)
  // #endif
  return { app }
}
