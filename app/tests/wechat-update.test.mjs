import { readFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'

const require = createRequire(import.meta.url)
const source = await readFile(new URL('../src/utils/wechatUpdate.ts', import.meta.url), 'utf8')
const exports = {}
const compiled = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.CommonJS } }).outputText
new Function('require', 'exports', compiled)(require, exports)
const { createWechatUpdateGuard, WECHAT_UPDATE_PATH } = exports

function fixture({ supported = true, navigationFails = false, applyFails = false } = {}) {
  const callbacks = {}
  const interceptors = new Map()
  const scheduled = new Map()
  const modals = []
  const routes = []
  const loading = []
  let route = 'pages/index/index'
  let applied = 0
  let managerReads = 0
  let timerId = 0
  let mask = false
  const manager = {
    onCheckForUpdate(fn) { callbacks.check = fn },
    onUpdateReady(fn) { callbacks.ready = fn },
    onUpdateFailed(fn) { callbacks.failed = fn },
    applyUpdate() { applied++; if (applyFails) throw new Error('apply failed') },
  }
  const platform = {
    canIUse: () => supported,
    getUpdateManager() { managerReads++; return manager },
    addInterceptor(api, hook) { interceptors.set(api, hook.invoke) },
    reLaunch(options) {
      assert.notEqual(interceptors.get('reLaunch')?.(options), false)
      routes.push(options.url)
      if (!navigationFails) route = options.url.slice(1)
      options.complete()
    },
    showLoading(options) { loading.push(options); mask = options.mask },
    hideLoading() { mask = false },
    showModal(options) { modals.push(options) },
  }
  const guard = createWechatUpdateGuard(platform, () => route, {
    setTimeout(fn, delay) { const id = ++timerId; scheduled.set(id, { fn, delay }); return id },
    clearTimeout(id) { scheduled.delete(id) },
  })
  return {
    guard, callbacks, modals, routes, loading, interceptors, scheduled, platform,
    route: () => route, applied: () => applied, masked: () => mask, managerReads: () => managerReads,
    setRoute(value) { route = value },
    settleModal(index = modals.length - 1, confirm = true) { modals[index].success({ confirm }); modals[index].complete() },
  }
}

test('没有新版本不打断业务；重复启动只注册一个管理器', () => {
  const f = fixture()
  f.guard.start(); f.guard.start()
  f.callbacks.check({ hasUpdate: false })
  f.guard.enforce()
  assert.equal(f.managerReads(), 1)
  assert.equal(f.guard.isBlocked(), false)
  assert.equal(f.guard.state.value, 'current')
  assert.equal(f.routes.length, 0)
  assert.equal(f.modals.length, 0)
})

test('发现新版本立即阻止旧页面及五类导航，下载中不能调用 applyUpdate', () => {
  const f = fixture(); f.guard.start()
  f.callbacks.check({ hasUpdate: true })
  assert.equal(f.guard.state.value, 'downloading')
  assert.equal(f.guard.isBlocked(), true)
  assert.equal(f.route(), WECHAT_UPDATE_PATH.slice(1))
  assert.equal(f.loading[0].mask, true)
  for (const api of ['navigateTo', 'redirectTo', 'switchTab', 'reLaunch', 'navigateBack']) {
    assert.equal(f.interceptors.get(api)({ url: '/pages/entry/entry' }), false, api)
  }
  assert.equal(f.interceptors.get('navigateBack')({}), false)
  f.guard.apply()
  assert.equal(f.applied(), 0)
  assert.equal(f.modals.length, 0)
})

test('下载完成只有重启更新选项，确认后应用一次，重启期间仍阻止旧版', () => {
  const f = fixture(); f.guard.start()
  f.callbacks.check({ hasUpdate: true }); f.callbacks.ready()
  assert.equal(f.scheduled.size, 0)
  assert.equal(f.modals.length, 1)
  assert.equal(f.modals[0].showCancel, false)
  assert.equal(f.modals[0].confirmText, '重启更新')
  assert.match(f.modals[0].content, /未保存/)
  f.guard.enforce(); f.callbacks.ready()
  assert.equal(f.modals.length, 1)
  f.settleModal()
  f.guard.apply(); f.callbacks.ready()
  assert.equal(f.applied(), 1)
  assert.equal(f.guard.state.value, 'restarting')
  assert.equal(f.guard.isBlocked(), true)
})

test('下载失败确认提示后不放行、不误调用 applyUpdate，前台恢复仍拦截', () => {
  const f = fixture(); f.guard.start()
  f.callbacks.check({ hasUpdate: true }); f.callbacks.failed()
  assert.equal(f.guard.state.value, 'failed')
  assert.match(f.modals[0].content, /关闭小程序后重新打开/)
  f.settleModal()
  assert.equal(f.applied(), 0)
  assert.equal(f.guard.isBlocked(), true)
  f.setRoute('pages/calendar/calendar'); f.guard.enforce()
  assert.equal(f.route(), WECHAT_UPDATE_PATH.slice(1))
  assert.equal(f.modals.length, 2)
})

test('60秒下载无结果提示恢复步骤，晚到的下载完成仍允许重启', () => {
  const f = fixture(); f.guard.start()
  f.callbacks.check({ hasUpdate: true })
  const [{ fn, delay }] = f.scheduled.values()
  assert.equal(delay, 60_000)
  fn()
  assert.equal(f.guard.state.value, 'failed')
  assert.match(f.guard.message.value, /超时/)
  f.settleModal(); f.callbacks.ready()
  assert.equal(f.guard.state.value, 'ready')
  f.settleModal()
  assert.equal(f.applied(), 1)
})

test('迟到的无更新/失败通知不释放拦截或覆盖已准备好的新包', () => {
  const f = fixture(); f.guard.start()
  f.callbacks.check({ hasUpdate: true }); f.callbacks.check({ hasUpdate: false })
  assert.equal(f.guard.state.value, 'downloading')
  f.callbacks.ready(); f.callbacks.failed(); f.callbacks.check({ hasUpdate: false })
  assert.equal(f.guard.state.value, 'ready')
  assert.equal(f.guard.isBlocked(), true)
})

test('原生弹窗失败或非确认关闭仍留在更新页，可再次提示', () => {
  const f = fixture(); f.guard.start(); f.callbacks.ready()
  f.modals[0].complete()
  assert.equal(f.guard.isBlocked(), true)
  f.guard.prompt()
  assert.equal(f.modals.length, 2)
  f.settleModal(1, false)
  assert.equal(f.applied(), 0)
  f.guard.apply() // 更新页的按钮可作为原生提示失败的回退入口。
  assert.equal(f.applied(), 1)
})

test('导航失败保留触摸遮罩，之后再次显示页面仍重试更新入口', () => {
  const f = fixture({ navigationFails: true }); f.guard.start()
  f.callbacks.check({ hasUpdate: true })
  assert.equal(f.masked(), true)
  f.guard.enforce()
  assert.equal(f.routes.length, 2)
  assert.equal(f.guard.isBlocked(), true)
})

test('重启异常仍保留更新拦截和明确的重新打开提示', () => {
  const f = fixture({ applyFails: true }); f.guard.start(); f.callbacks.ready(); f.settleModal()
  assert.equal(f.guard.state.value, 'failed')
  assert.equal(f.guard.isBlocked(), true)
  assert.match(f.guard.message.value, /重启更新失败/)
})

test('不支持更新管理器/初始化异常均引导恢复，不静默放行', () => {
  const unsupported = fixture({ supported: false }); unsupported.guard.start()
  assert.equal(unsupported.guard.state.value, 'unsupported')
  assert.match(unsupported.guard.message.value, /升级微信/)
  assert.equal(unsupported.managerReads(), 0)
  const failed = fixture()
  failed.platform.getUpdateManager = () => { throw new Error('unavailable') }
  failed.guard.start()
  assert.equal(failed.guard.state.value, 'failed')
  assert.equal(failed.guard.isBlocked(), true)
  const capabilityFailed = fixture()
  capabilityFailed.platform.canIUse = () => { throw new Error('capability unavailable') }
  capabilityFailed.guard.start()
  assert.equal(capabilityFailed.guard.state.value, 'failed')
  assert.equal(capabilityFailed.guard.isBlocked(), true)
})

test('应用注册早于异步恢复，页面与前台均接入，H5条件编译移除更新逻辑', async () => {
  const app = await readFile(new URL('../src/App.vue', import.meta.url), 'utf8')
  const main = await readFile(new URL('../src/main.ts', import.meta.url), 'utf8')
  assert.match(app, /onLaunch\(async \(\) => \{\s*\/\/ #ifdef MP-WEIXIN\s*wechatUpdate.start\(\)\s*\/\/ #endif\s*await ledger.restore/)
  assert.match(app, /onShow\(\(\) => wechatUpdate.enforce\(\)\)/)
  assert.match(main, /app.mixin\(\{ onShow\(\) \{ getWechatUpdateGuard\(\).enforce\(\) \}/)
  const h5 = value => value.replace(/\/\/ #ifdef MP-WEIXIN\n[\s\S]*?\/\/ #endif/g, '')
  assert.doesNotMatch(h5(app) + h5(main), /wechatUpdate|getWechatUpdateGuard/)
  const pages = JSON.parse(await readFile(new URL('../src/pages.json', import.meta.url), 'utf8'))
  assert.ok(pages.pages.some(page => `/${page.path}` === WECHAT_UPDATE_PATH))
  assert.ok(pages.tabBar.list.every(tab => `/${tab.pagePath}` !== WECHAT_UPDATE_PATH))
})
