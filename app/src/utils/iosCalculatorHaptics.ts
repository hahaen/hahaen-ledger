// 仅由 H5 金额键盘加载；switch 存在不代表设备一定产生物理触感。
export function supportsIosCalculatorHaptics(): boolean {
  if (typeof navigator === 'undefined' || typeof document === 'undefined') return false
  const ua = navigator.userAgent
  return /iPhone/.test(ua) && /Version\/[\d.]+.*Safari\//.test(ua)
    && 'switch' in document.createElement('input')
}
