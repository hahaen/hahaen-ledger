// #ifdef MP-WEIXIN
declare const wx: { env: { USER_DATA_PATH: string } }
// #endif

export const WECHAT_SHARE_TITLE = '哈记账｜简单记账，安心生活'
export const WECHAT_SHARE_PATH = '/pages/index/index'
export const WECHAT_SHARE_IMAGE = '/static/share-welcome.png'
let shareImagePath = WECHAT_SHARE_IMAGE

function prepareWechatShareImage() {
  // #ifdef MP-WEIXIN
  if (shareImagePath !== WECHAT_SHARE_IMAGE) return
  try {
    const destination = `${wx.env.USER_DATA_PATH}/share-welcome.png`
    uni.getFileSystemManager().copyFileSync(WECHAT_SHARE_IMAGE, destination)
    shareImagePath = destination
  } catch {
    // 保留固定代码包图片作为回退，绝不使用当前页面截图。
    console.warn('微信固定分享图片准备失败，使用代码包图片')
  }
  // #endif
}

export function createWechatShareMessage() {
  return {
    title: WECHAT_SHARE_TITLE,
    path: WECHAT_SHARE_PATH,
    imageUrl: shareImagePath,
  }
}

// 全局选项由 uni-app 原生页面初始化识别，不能藏在 setup 的工具回调里。
export const wechatShareMixin = {
  onLoad: prepareWechatShareImage,
  onShareAppMessage: createWechatShareMessage,
}
