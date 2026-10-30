interface H5UpdatePlatform {
  currentVersion: string
  readVersion(): Promise<unknown>
  isVisible(): boolean
  onRequired(source: 'update' | 'manual'): void
  navigate(version: string): void
}

export function parseH5Version(value: unknown): string | undefined {
  if (!value || typeof value !== 'object' || !('version' in value)) return
  const version = value.version
  if (typeof version === 'string' && /^[a-zA-Z0-9_-]{1,128}$/.test(version)) return version
}

// 不依赖 Vue/DOM 的状态机；同一次探测合并，发现更新后始终保持拦截。
export function createH5UpdateGuard(platform: H5UpdatePlatform, now = Date.now) {
  let checking: Promise<void> | undefined
  let lastChecked = -Infinity
  let required = false
  let refreshing = false

  function requireRefresh(source: 'update' | 'manual' = 'manual') {
    if (required) return
    required = true
    platform.onRequired(source)
  }

  async function check() {
    if (required || !platform.isVisible()) return
    if (checking) return checking
    if (now() - lastChecked < 5000) return
    lastChecked = now()
    checking = (async () => {
      try {
        const version = parseH5Version(await platform.readVersion())
        if (version && version !== platform.currentVersion) {
          requireRefresh('update')
        }
      } catch {
        // 探测失败不代表发布更新，下一次前台/联网/定时检测再试。
      }
    })()
    try { await checking } finally { checking = undefined }
  }

  async function refresh() {
    if (!required || refreshing) return
    refreshing = true
    try {
      const version = parseH5Version(await platform.readVersion())
      if (!version) throw new Error('无效的版本响应')
      platform.navigate(version)
    } catch (error) {
      refreshing = false
      throw error
    }
  }

  return { check, refresh, requireRefresh }
}

export function h5RefreshUrl(href: string, version: string, timestamp: number): string {
  const url = new URL(href)
  url.searchParams.set('__haji_v', `${version}_${timestamp}`)
  return url.href
}

export async function fetchH5Version(url: URL, fetcher = fetch, timeoutMs = 8000): Promise<unknown> {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), timeoutMs)
  try {
    const response = await fetcher(url, { cache: 'no-store', signal: controller.signal })
    if (!response.ok) throw new Error('版本检测暂不可用')
    return await response.json()
  } finally { clearTimeout(timeout) }
}
