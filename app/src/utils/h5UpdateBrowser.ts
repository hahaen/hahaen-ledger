import { createApp as createVueApp } from 'vue'
import H5RequiredRefresh from '../components/H5RequiredRefresh.vue'
import { createH5UpdateGuard, fetchH5Version, h5RefreshUrl } from './h5Update'

let guard: ReturnType<typeof createH5UpdateGuard> | undefined

export function installH5Update(currentVersion: string) {
  if (guard) return guard
  const updateGuard = createH5UpdateGuard({
    currentVersion,
    isVisible: () => document.visibilityState === 'visible',
    readVersion: () => {
      // DEV不生成版本文件，手动入口仅重载当前开发页面。
      if (!currentVersion) return Promise.resolve({ version: 'development' })
      const url = new URL('haji-version.json', new URL(import.meta.env.BASE_URL, window.location.origin))
      url.searchParams.set('_', String(Date.now()))
      return fetchH5Version(url)
    },
    onRequired: source => {
      const host = document.createElement('div')
      document.body.appendChild(host)
      createVueApp(H5RequiredRefresh, { refresh: updateGuard.refresh, manual: source === 'manual' }).mount(host)
    },
    navigate: version => window.location.replace(h5RefreshUrl(window.location.href, version, Date.now())),
  })
  guard = updateGuard
  if (currentVersion) {
    document.addEventListener('visibilitychange', guard.check)
    window.addEventListener('focus', guard.check)
    window.addEventListener('pageshow', guard.check)
    window.addEventListener('online', guard.check)
    window.setInterval(guard.check, 60_000)
    void guard.check()
  }
  return guard
}

export function requestH5Refresh() {
  installH5Update(__H5_BUILD_VERSION__).requireRefresh()
}
