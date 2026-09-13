import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'

const source = await readFile(new URL('../src/utils/h5AuthGuard.ts', import.meta.url), 'utf8')
const compiled = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ESNext, module: ts.ModuleKind.ESNext } }).outputText
const moduleUrl = `data:text/javascript;base64,${Buffer.from(compiled).toString('base64')}`
const { currentH5Path, installH5AuthGuard, isH5AuthPath, shouldRedirectAuthenticatedH5UserToHome } = await import(moduleUrl)

function browser(hash = '') {
  const listeners = new Map()
  return {
    location: { hash, pathname: '/' },
    addEventListener(type, listener) { listeners.set(type, listener) },
    removeEventListener(type) { listeners.delete(type) },
    emit(type) { listeners.get(type)?.() },
    listenerCount() { return listeners.size },
  }
}

test('H5 路由识别会保留 hash 路径并允许登录和注册页', () => {
  assert.equal(currentH5Path({ hash: '#/pages/calendar/calendar?month=2026-09', pathname: '/' }), '/pages/calendar/calendar')
  assert.equal(currentH5Path({ hash: '', pathname: '/' }), '/')
  assert.equal(isH5AuthPath('/pages/auth/login/login/'), true)
  assert.equal(isH5AuthPath('/pages/auth/register/register?from=logout'), true)
  assert.equal(isH5AuthPath('/pages/index/index'), false)
})

test('已恢复会话重新打开默认认证页时进入首页，业务页和协议页保持原路由', () => {
  assert.equal(shouldRedirectAuthenticatedH5UserToHome('/'), true)
  assert.equal(shouldRedirectAuthenticatedH5UserToHome('/pages/auth/login/login'), true)
  assert.equal(shouldRedirectAuthenticatedH5UserToHome('/pages/auth/register/register?from=login'), true)
  assert.equal(shouldRedirectAuthenticatedH5UserToHome('/pages/index/index'), false)
  assert.equal(shouldRedirectAuthenticatedH5UserToHome('/pages/legal/privacy/privacy'), false)
})

test('无会话访问业务路由时只重定向一次，认证页和有效会话不拦截', () => {
  const browserWindow = browser('#/pages/index/index')
  let hasSession = false
  let redirects = 0
  const guard = installH5AuthGuard({ hasSession: () => hasSession, redirectToLogin: () => { redirects++ } }, browserWindow)

  guard.enforce()
  browserWindow.emit('hashchange')
  assert.equal(redirects, 1)

  browserWindow.location.hash = '#/pages/auth/login/login'
  browserWindow.emit('hashchange')
  browserWindow.location.hash = '#/pages/auth/register/register'
  browserWindow.emit('hashchange')
  assert.equal(redirects, 1)

  hasSession = true
  browserWindow.location.hash = '#/pages/assets/assets'
  browserWindow.emit('popstate')
  assert.equal(redirects, 1)
  guard.dispose()
  assert.equal(browserWindow.listenerCount(), 0)
})

test('退出后恢复浏览器历史到业务页仍会被收口到登录页', () => {
  const browserWindow = browser('#/pages/detail/detail?id=42')
  let redirects = 0
  const guard = installH5AuthGuard({ hasSession: () => false, redirectToLogin: () => { redirects++ } }, browserWindow)
  browserWindow.emit('pageshow')
  assert.equal(redirects, 1)
  guard.dispose()
})

test('H5 手动注销会回到登录页，App 全局安装认证守卫', async () => {
  const appSource = await readFile(new URL('../src/App.vue', import.meta.url), 'utf8')
  const mineSource = await readFile(new URL('../src/pages/mine/mine.vue', import.meta.url), 'utf8')
  assert.match(appSource, /installH5AuthGuard/)
  assert.match(appSource, /shouldRedirectAuthenticatedH5UserToHome\(currentPath\)/)
  assert.match(mineSource, /typeof window !== 'undefined'\) uni\.reLaunch\(\{ url: '\/pages\/auth\/login\/login' \}\)/)
})
