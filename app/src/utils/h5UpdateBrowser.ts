import { createApp as createVueApp } from 'vue'
import H5RequiredRefresh from '../components/H5RequiredRefresh.vue'
import { createH5UpdateGuard, fetchH5Version, h5RefreshUrl } from './h5Update'

let installed = false

export function installH5Update(currentVersion: string) {
  if (installed) return
  installed = true
  const guard = createH5UpdateGuard({
    currentVersion,
    isVisible: () => document.visibilityState === 'visible',
    readVersion: () => {
      const url = new URL('haji-version.json', new URL(import.meta.env.BASE_URL, window.location.origin))
      url.searchParams.set('_', String(Date.now()))
      return fetchH5Version(url)
    },
    onRequired: () => {
      const host = document.createElement('div')
      document.body.appendChild(host)
      createVueApp(H5RequiredRefresh, { refresh: guard.refresh }).mount(host)
    },
    navigate: version => window.location.replace(h5RefreshUrl(window.location.href, version, Date.now())),
  })
  document.addEventListener('visibilitychange', guard.check)
  window.addEventListener('focus', guard.check)
  window.addEventListener('pageshow', guard.check)
  window.addEventListener('online', guard.check)
  window.setInterval(guard.check, 60_000)
  void guard.check()
}
