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

// 独立物品域，所有页面请求统一经此入口。
import type { EditItemPayload, Item, ItemDetail, ItemOverview, ItemPayload, RetirePayload } from './items'
export const itemApi = {
  list: (status: string, page = 1) => request<ItemOverview>(`/api/app/items?status=${encodeURIComponent(status)}&page=${page}&pageSize=20`),
  detail: (id: string) => request<ItemDetail>(`/api/app/items/${encodeURIComponent(id)}`),
  create: (data: ItemPayload) => request<Item>('/api/app/items', { method: 'POST', data }),
  retire: (id: string, data: RetirePayload) => request<Item>(`/api/app/items/${encodeURIComponent(id)}/retire`, { method: 'POST', data }),
  reactivate: (id: string, idempotencyKey: string) => request<Item>(`/api/app/items/${encodeURIComponent(id)}/reactivate`, { method: 'POST', data: { idempotencyKey } }),
  edit: (id: string, data: EditItemPayload) => request<Item>(`/api/app/items/${encodeURIComponent(id)}`, { method: 'PUT', data }),
  remove: (id: string, key: string) => request<void>(`/api/app/items/${encodeURIComponent(id)}?idempotencyKey=${encodeURIComponent(key)}`, { method: 'DELETE' }),
}

export type NotificationType = 'BARK' | 'PUSHPLUS'
export type NotificationConfig = { notificationType: string; configured: boolean; notificationKey: string | null }
export const notificationConfigApi = {
  list: () => request<NotificationConfig[]>('/api/app/user/notification-configs'),
  save: (type: NotificationType, data: { encryptedKey?: string; remove: boolean; idempotencyKey: string }) =>
    request<NotificationConfig>(`/api/app/user/notification-configs/${type}`, { method: 'PUT', data }),
}

export type TodoRecurrence = 'ONCE' | 'DAILY' | 'MONTHLY' | 'EVERY_N_MONTHS' | 'YEARLY'
export type TodoStatus = 'PENDING' | 'COMPLETED'
export type TodoItem = {
  id: string; ruleId: string; title: string; note: string | null; recurrence: TodoRecurrence
  monthInterval: number; dueAt: string; anchorAt: string; remind: boolean; status: TodoStatus; completedAt: string | null
}
export type TodoPage = { pendingCount: number; completedCount: number; items: TodoItem[]; hasMore: boolean }
export type TodoPayload = {
  title: string; note: string; recurrence: TodoRecurrence; monthInterval: number
  dueAt: string; remind: boolean; idempotencyKey: string
}
export type TodoAttempt = { channel: string; messageTitle: string; messageBody: string; result: string; attemptedAt: string }
export const todoApi = {
  detail: (id: string) => request<TodoItem>(`/api/app/todos/${encodeURIComponent(id)}`),
  attempts: (id: string) => request<TodoAttempt[]>(`/api/app/todos/${encodeURIComponent(id)}/attempts`),
  list: (status: TodoStatus, page = 1) => request<TodoPage>(`/api/app/todos?status=${status}&page=${page}&pageSize=20`),
  create: (data: TodoPayload) => request<string>('/api/app/todos', { method: 'POST', data }),
  edit: (id: string, data: TodoPayload) => request<void>(`/api/app/todos/${encodeURIComponent(id)}`, { method: 'PUT', data }),
  complete: (id: string, idempotencyKey: string) => request<void>(`/api/app/todos/${encodeURIComponent(id)}/complete`, { method: 'POST', data: { idempotencyKey } }),
  remove: (id: string, idempotencyKey: string) => request<void>(`/api/app/todos/${encodeURIComponent(id)}/delete`, { method: 'POST', data: { idempotencyKey } }),
}
