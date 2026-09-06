export type AppEnv = 'development' | 'production'

const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim()
const appEnv: AppEnv = import.meta.env.VITE_APP_ENV === 'production' || import.meta.env.MODE === 'production'
  ? 'production'
  : 'development'

if (appEnv === 'production' && !configuredApiBaseUrl) {
  throw new Error('生产环境必须配置 VITE_API_BASE_URL')
}

export const runtimeConfig = {
  env: appEnv,
  apiBaseUrl: (configuredApiBaseUrl || 'http://127.0.0.1:8080').replace(/\/+$/, ''),
}
