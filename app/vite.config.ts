import { defineConfig, loadEnv } from 'vite'
import { fileURLToPath, URL } from 'node:url'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig(({ mode }) => {
  const envDir = fileURLToPath(new URL('./env', import.meta.url))
  const env = loadEnv(mode, envDir, 'VITE_')
  if (mode === 'production' && !env.VITE_API_BASE_URL?.trim()) {
    throw new Error('生产构建缺少 VITE_API_BASE_URL，请配置 app/env/.env.production 或构建环境变量')
  }

  return {
    envDir,
    plugins: [uni()],
  }
})
