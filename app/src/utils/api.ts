import { runtimeConfig } from './env'

export type ApiResponse<T> = { code: number; message: string; data: T }

export function request<T>(url: string, options: Omit<UniApp.RequestOptions, 'url'> = {}) : Promise<T> {
  const token = uni.getStorageSync('auth-token')
  return new Promise((resolve, reject) => {
    uni.request({ ...options, url: `${runtimeConfig.apiBaseUrl}${url}`, header: { ...(options.header || {}), ...(token ? { 'X-Auth-Token': token } : {}) }, success: (res) => { const body = res.data as ApiResponse<T>; if (res.statusCode === 401 || res.statusCode === 403) { uni.removeStorageSync('auth-token'); uni.showToast({ title: '登录已失效，请重新进入', icon: 'none' }) } if (!body || body.code !== 0) { uni.showToast({ title: body?.message || '请求失败，请重试', icon: 'none' }); reject(new Error(body?.message || '请求失败')); return } resolve(body.data) }, fail: (error) => { uni.showToast({ title: '网络异常，请重试', icon: 'none' }); reject(error) } })
  })
}
