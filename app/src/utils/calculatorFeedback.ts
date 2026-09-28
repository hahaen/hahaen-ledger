export function triggerCalculatorFeedback() {
  // #ifdef MP-WEIXIN
  uni.vibrateShort({ type: 'light', fail: () => {} })
  // #endif
  // #ifdef H5
  if (typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function') {
    if (typeof window === 'undefined' || !window.matchMedia('(prefers-reduced-motion: reduce)').matches) navigator.vibrate(8)
  }
  // #endif
}
