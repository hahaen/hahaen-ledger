import { staticResource } from './staticResource'

export const WECHAT_SHARE_TITLE = '哈记账｜简单记账，安心生活'
export const WECHAT_SHARE_PATH = '/pages/index/index'
export const WECHAT_SHARE_IMAGE = staticResource('brand.png')

export function createWechatShareMessage() {
  return {
    title: WECHAT_SHARE_TITLE,
    path: WECHAT_SHARE_PATH,
    imageUrl: WECHAT_SHARE_IMAGE,
  }
}

// 全局选项由 uni-app 原生页面初始化识别，不能藏在 setup 的工具回调里。
export const wechatShareMixin = {
  onShareAppMessage: createWechatShareMessage,
}
