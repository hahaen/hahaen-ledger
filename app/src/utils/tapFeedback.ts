const TAP_TARGET_SELECTOR = 'button, [role="button"], switch, picker, a[href], .account-reorder-action'
let lastFeedbackAt = 0

function isDisabled(element: Element) {
  return element.matches(':disabled, [disabled], [aria-disabled="true"]')
}

export function triggerTapFeedback() {
  const now = Date.now()
  if (now - lastFeedbackAt < 40) return
  lastFeedbackAt = now
  // #ifdef MP-WEIXIN
  uni.vibrateShort({ type: 'light', fail: () => {} })
  // #endif
  // #ifdef H5
  if (typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function') {
    if (typeof window === 'undefined' || !window.matchMedia('(prefers-reduced-motion: reduce)').matches) navigator.vibrate(8)
  }
  // #endif
}

export function installH5TapFeedback() {
  // #ifdef H5
  if (typeof document === 'undefined') return () => {}
  let lastFeedbackAt = 0
  const handlePointerDown = (event: Event) => {
    const target = event.target
    if (!(target instanceof Element)) return
    const interactive = target.closest(TAP_TARGET_SELECTOR)
    if (!interactive || isDisabled(interactive)) return
    const now = Date.now()
    if (now - lastFeedbackAt < 40) return
    lastFeedbackAt = now
    triggerTapFeedback()
  }
  document.addEventListener('pointerdown', handlePointerDown, true)
  return () => document.removeEventListener('pointerdown', handlePointerDown, true)
  // #endif
  return () => {}
}
