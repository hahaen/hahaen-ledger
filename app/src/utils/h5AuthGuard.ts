export const H5_LOGIN_PATH = '/pages/auth/login/login'
export const H5_REGISTER_PATH = '/pages/auth/register/register'
export const H5_LEGAL_PATHS = ['/pages/legal/agreement/agreement', '/pages/legal/privacy/privacy'] as const

type H5Location = { hash: string; pathname: string }
type H5Window = {
  location: H5Location
  addEventListener: (type: string, listener: () => void) => void
  removeEventListener: (type: string, listener: () => void) => void
}

function normalizePath(path: string) {
  const withoutQuery = path.split('?')[0] || '/'
  if (withoutQuery.length > 1) return withoutQuery.replace(/\/$/, '')
  return withoutQuery
}

export function currentH5Path(location: H5Location) {
  const hashPath = location.hash.replace(/^#/, '')
  return normalizePath(hashPath || location.pathname || '/')
}

export function isH5AuthPath(path: string) {
  const normalizedPath = normalizePath(path)
  return normalizedPath === H5_LOGIN_PATH || normalizedPath === H5_REGISTER_PATH || H5_LEGAL_PATHS.includes(normalizedPath as typeof H5_LEGAL_PATHS[number])
}

/** 已恢复会话时，H5 的默认入口和认证表单统一回到首页。 */
export function shouldRedirectAuthenticatedH5UserToHome(path: string) {
  const normalizedPath = normalizePath(path)
  return normalizedPath === '/' || normalizedPath === H5_LOGIN_PATH || normalizedPath === H5_REGISTER_PATH
}

export type H5AuthGuard = {
  enforce: () => void
  dispose: () => void
}

export function installH5AuthGuard(options: { hasSession: () => boolean; redirectToLogin: () => void }, browserWindow?: H5Window): H5AuthGuard {
  const currentWindow = browserWindow || (typeof window !== 'undefined' ? window : undefined)
  let redirecting = false

  const enforce = () => {
    if (!currentWindow) return
    const authPage = isH5AuthPath(currentH5Path(currentWindow.location))
    if (options.hasSession() || authPage) {
      redirecting = false
      return
    }
    if (!redirecting) {
      redirecting = true
      options.redirectToLogin()
    }
  }

  if (currentWindow) {
    currentWindow.addEventListener('hashchange', enforce)
    currentWindow.addEventListener('popstate', enforce)
    currentWindow.addEventListener('pageshow', enforce)
  }

  return {
    enforce,
    dispose: () => {
      if (!currentWindow) return
      currentWindow.removeEventListener('hashchange', enforce)
      currentWindow.removeEventListener('popstate', enforce)
      currentWindow.removeEventListener('pageshow', enforce)
    },
  }
}
