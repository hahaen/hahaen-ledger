import { runtimeConfig } from './env'

export type ApiResponse<T> = { code: number; message: string; data: T }

type AuthExpiredHandler = () => void | Promise<void>
let authExpiredHandler: AuthExpiredHandler | undefined

/** 注册全局会话失效处理，避免只清 localStorage 而留下错误的响应式登录态。 */
export function setAuthExpiredHandler(handler: AuthExpiredHandler | undefined) {
  authExpiredHandler = handler
}

export function request<T>(url: string, options: Omit<UniApp.RequestOptions, 'url'> = {}) : Promise<T> {
  return requestWithAuthRetry<T>(url, options, true)
}

function requestWithAuthRetry<T>(url: string, options: Omit<UniApp.RequestOptions, 'url'>, retryAfterAuthExpiry: boolean): Promise<T> {
  const token = uni.getStorageSync('auth-token')
  return new Promise((resolve, reject) => {
    uni.request({ ...options, url: `${runtimeConfig.apiBaseUrl}${url}`, header: { ...(options.header || {}), ...(token ? { 'X-Auth-Token': token } : {}) }, success: async (res) => {
      const body = res.data as ApiResponse<T>
      if ((res.statusCode === 401 || res.statusCode === 403) && retryAfterAuthExpiry) {
        uni.removeStorageSync('auth-token')
        uni.removeStorageSync('auth-user')
        try {
          const recovery = authExpiredHandler?.()
          if (recovery) {
            await recovery
            resolve(requestWithAuthRetry<T>(url, options, false))
            return
          }
        } catch {
          // 重新认证失败时继续走统一错误处理，避免把失败伪装成成功。
        }
      }
      if (res.statusCode === 401 || res.statusCode === 403) {
        uni.showToast({ title: '登录已失效，请重新进入', icon: 'none' })
      }
      if (!body || body.code !== 0) { uni.showToast({ title: body?.message || '请求失败，请重试', icon: 'none' }); reject(new Error(body?.message || '请求失败')); return }
      resolve(body.data)
    }, fail: (error) => { uni.showToast({ title: '网络异常，请重试', icon: 'none' }); reject(error) } })
  })
}
