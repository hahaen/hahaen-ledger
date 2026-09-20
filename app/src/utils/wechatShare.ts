import { onShareAppMessage } from '@dcloudio/uni-app'

export const WECHAT_SHARE_TITLE = '哈记账｜简单记账，安心生活'
export const WECHAT_SHARE_PATH = '/pages/index/index'

export function createWechatShareMessage() {
  return {
    title: WECHAT_SHARE_TITLE,
    path: WECHAT_SHARE_PATH,
  }
}

export function registerWechatShare() {
  // #ifdef MP-WEIXIN
  onShareAppMessage(() => createWechatShareMessage())
  // #endif
}
