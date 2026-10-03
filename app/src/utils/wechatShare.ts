import { staticResource } from './staticResource'

export const WECHAT_SHARE_TITLE = '哈记账｜简单记账，安心生活'
export const WECHAT_SHARE_PATH = '/pages/index/index'
export const WECHAT_SHARE_IMAGE = staticResource('share-welcome.png')

export function createWechatShareMessage() {
  return {
    title: WECHAT_SHARE_TITLE,
    path: WECHAT_SHARE_PATH,
    imageUrl: WECHAT_SHARE_IMAGE,
  }
}
