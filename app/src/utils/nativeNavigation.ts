export type NativeMenuMetrics = {
  top: number
  height: number
  left: number
  right: number
  rootInset: number
  rightPadding: number
}

/** Read the system menu button geometry on WeChat so custom headers can share its row. */
export function getNativeMenuMetrics(): NativeMenuMetrics | null {
  // #ifdef MP-WEIXIN
  try {
    const system = uni.getSystemInfoSync()
    const menu = uni.getMenuButtonBoundingClientRect()
    if (!menu || !menu.height || !system.windowWidth) return null
    const rootInset = Math.max(system.statusBarHeight || 0, system.safeArea?.top || 0)
    return {
      top: menu.top,
      height: menu.height,
      left: menu.left,
      right: menu.right,
      rootInset,
      rightPadding: Math.max(16, system.windowWidth - menu.left - 4),
    }
  } catch {
    return null
  }
  // #endif
  return null
}
