import { readonly, ref } from 'vue'

export const WECHAT_UPDATE_PATH = '/pages/required-update/required-update'

export type UpdateState = 'checking' | 'current' | 'downloading' | 'ready' | 'restarting' | 'failed' | 'unsupported'

interface UpdateManager {
  onCheckForUpdate(callback: (result: { hasUpdate: boolean }) => void): void
  onUpdateReady(callback: () => void): void
  onUpdateFailed(callback: () => void): void
  applyUpdate(): void
}

interface NavigationOptions { url?: string }
interface UpdatePlatform {
  canIUse(api: string): boolean
  getUpdateManager(): UpdateManager
  addInterceptor(api: string, interceptor: { invoke(options: NavigationOptions): boolean | void }): void
  reLaunch(options: { url: string; complete: () => void }): void
  showLoading(options: { title: string; mask: boolean }): void
  hideLoading(): void
  showModal(options: {
    title: string; content: string; showCancel: boolean; confirmText: string; confirmColor: string
    success(result: { confirm: boolean }): void
    complete(): void
  }): void
}

// 可替换的只有平台边界，状态转换和所有回调始终执行同一份正式实现。
export function createWechatUpdateGuard(
  platform: UpdatePlatform,
  currentRoute: () => string,
  timers = { setTimeout, clearTimeout },
) {
  const state = ref<UpdateState>('checking')
  const message = ref('')
  let manager: UpdateManager | undefined
  let started = false
  let redirecting = false
  let prompting = false
  let masked = false
  let downloadTimeout: ReturnType<typeof setTimeout> | undefined

  const isBlocked = () => state.value !== 'checking' && state.value !== 'current'
  const onUpdatePage = () => `/${currentRoute().replace(/^\//, '')}` === WECHAT_UPDATE_PATH
  function clearDownloadTimeout() {
    if (downloadTimeout !== undefined) timers.clearTimeout(downloadTimeout)
    downloadTimeout = undefined
  }
  function mask() {
    masked = true
    platform.showLoading({ title: '需要更新后使用', mask: true })
  }
  function unmask() {
    if (masked) platform.hideLoading()
    masked = false
  }
  function fail(content: string) {
    clearDownloadTimeout()
    state.value = 'failed'
    message.value = content
    enforce()
  }
  function apply() {
    if (state.value !== 'ready' || !manager) return
    state.value = 'restarting'
    message.value = '正在重启并应用新版本，请稍候。'
    try { manager.applyUpdate() }
    catch { fail('重启更新失败，请关闭小程序后重新打开。旧版本暂不可继续使用。') }
  }
  function prompt() {
    if (prompting || !['ready', 'failed', 'unsupported'].includes(state.value)) return
    const ready = state.value === 'ready'
    prompting = true
    platform.showModal({
      title: ready ? '需要更新' : '暂不可使用',
      content: message.value,
      showCancel: false,
      confirmText: ready ? '重启更新' : '我知道了',
      confirmColor: '#278879',
      success: result => { if (result.confirm && ready) apply() },
      // 平台弹窗失败也不放行：更新页面及入口按钮始终保留。
      complete: () => { prompting = false },
    })
  }
  function enforce() {
    if (!isBlocked()) return
    if (onUpdatePage()) {
      unmask()
      prompt()
      return
    }
    if (redirecting) return
    redirecting = true
    mask()
    platform.reLaunch({
      url: WECHAT_UPDATE_PATH,
      complete: () => {
        redirecting = false
        // 导航失败保留触摸遮罩；前台恢复或下一次页面显示会再次尝试。
        if (onUpdatePage()) { unmask(); prompt() }
      },
    })
  }
  function start() {
    if (started) return
    started = true
    for (const api of ['navigateTo', 'redirectTo', 'switchTab', 'reLaunch', 'navigateBack']) {
      platform.addInterceptor(api, {
        invoke: options => {
          if (!isBlocked() || options.url?.split('?')[0] === WECHAT_UPDATE_PATH) return
          enforce()
          return false
        },
      })
    }
    try {
      if (!platform.canIUse('getUpdateManager')) {
        state.value = 'unsupported'
        message.value = '当前微信版本不支持检查小程序更新，请升级微信后重新打开。'
        enforce()
        return
      }
      manager = platform.getUpdateManager()
      // 先注册下载事件，避免仅在 hasUpdate 回调里注册而遗漏通知。
      manager.onUpdateReady(() => {
        if (state.value === 'restarting') return
        clearDownloadTimeout()
        state.value = 'ready'
        message.value = '新版本已准备好，需要重启更新后才能继续使用。重启会关闭当前页面，未保存的内容不会保留。'
        enforce()
      })
      manager.onUpdateFailed(() => {
        if (state.value === 'ready' || state.value === 'restarting') return
        fail('新版本下载失败，请检查网络，关闭小程序后重新打开。旧版本暂不可继续使用。')
      })
      manager.onCheckForUpdate(({ hasUpdate }) => {
        if (!hasUpdate) {
          // 迟到的“无更新”通知不能释放已发现旧版本的拦截。
          if (state.value === 'checking') state.value = 'current'
          return
        }
        if (state.value === 'ready' || state.value === 'restarting' || state.value === 'downloading') return
        state.value = 'downloading'
        message.value = '发现新版本，正在下载。下载完成后需重启更新才能继续使用。'
        clearDownloadTimeout()
        downloadTimeout = timers.setTimeout(() => {
          if (state.value === 'downloading') fail('新版本下载超时，请检查网络，关闭小程序后重新打开。旧版本暂不可继续使用。')
        }, 60_000)
        enforce()
      })
    } catch {
      fail('无法检查小程序更新，请关闭小程序后重新打开；仍无法检查时，请升级微信。')
    }
  }
  return { state: readonly(state), message: readonly(message), start, enforce, apply, prompt, isBlocked }
}

let guard: ReturnType<typeof createWechatUpdateGuard> | undefined
export function getWechatUpdateGuard() {
  return guard ||= createWechatUpdateGuard(uni, () => {
    const pages = getCurrentPages()
    return pages[pages.length - 1]?.route || ''
  })
}
