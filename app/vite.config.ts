import { defineConfig, loadEnv, type Plugin } from 'vite'
import { fileURLToPath, URL } from 'node:url'
import uni from '@dcloudio/vite-plugin-uni'

function forgeWechatGlobalScope(): Plugin {
  return {
    name: 'forge-wechat-global-scope',
    enforce: 'pre',
    transform(code, id) {
      if (id.includes('?') || !id.replaceAll('\\', '/').endsWith('/node-forge/lib/util.js')) return
      const original = "return typeof self === 'undefined' ? window : self;"
      if (!code.includes(original)) throw new Error('node-forge 全局对象初始化代码已变化，请重新核对微信兼容补丁')
      return code.replace(original, 'return globalThis;')
    },
  }
}

export default defineConfig(({ mode }) => {
  const envDir = fileURLToPath(new URL('./env', import.meta.url))
  const env = loadEnv(mode, envDir, 'VITE_')
  if (mode === 'production' && !env.VITE_API_BASE_URL?.trim()) {
    throw new Error('生产构建缺少 VITE_API_BASE_URL，请配置 app/env/.env.production 或构建环境变量')
  }

  return {
    envDir,
    base: '/haji/',
    plugins: [forgeWechatGlobalScope(), uni()],
    server: {
      port: 5180,
      strictPort: true,
    },
  }
})
