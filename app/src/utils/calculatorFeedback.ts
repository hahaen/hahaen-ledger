export function triggerCalculatorFeedback() {
  // #ifdef MP-WEIXIN
  uni.vibrateShort({ type: 'light', fail: () => {
    // 旧微信/设备不支持 light 时使用默认短振动。
    uni.vibrateShort({ fail: () => {} })
  } })
  // #endif
  // #ifdef H5
  // 浏览器支持时在真实点击中调用；无振动 API 的浏览器保留按压视觉反馈。
  if (typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function') navigator.vibrate(15)
  // #endif
}
