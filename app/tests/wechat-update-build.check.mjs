// 两端构建后执行；加载实际生产包，替换微信/应用外部边界，检查编译接线。
import { readFileSync, readdirSync } from 'node:fs'
import { resolve } from 'node:path'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'

const require = createRequire(import.meta.url)
const vue = require('vue')
const root = resolve(import.meta.dirname, '../dist/build')
const events = []
const callbacks = {}
const modals = []
const interceptors = new Map()
let route = 'pages/index/index'
let applied = 0
let launch, show, pageShow, appComponent
const platform = {
  canIUse: () => true,
  getUpdateManager() {
    events.push('manager')
    return {
      onCheckForUpdate(fn) { callbacks.check = fn },
      onUpdateReady(fn) { callbacks.ready = fn },
      onUpdateFailed(fn) { callbacks.failed = fn },
      applyUpdate() { applied++ },
    }
  },
  addInterceptor(api, hook) { interceptors.set(api, hook.invoke) },
  reLaunch(options) { route = options.url.slice(1); options.complete() },
  showLoading() {}, hideLoading() {},
  showModal(options) { modals.push(options) },
}
const vendor = {
  ...vue, index: platform,
  defineComponent(value) { appComponent = value; return value },
  onLaunch(fn) { launch = fn }, onShow(fn) { show = fn },
  createSSRApp() { return { mixin(value) { pageShow = value.onShow }, mount() {} } },
}
const cache = new Map()
function load(path) {
  if (cache.has(path)) return cache.get(path)
  const module = { exports: {} }
  new Function('require', 'module', 'exports', 'getCurrentPages', readFileSync(path, 'utf8'))(name => {
    if (name.endsWith('common/vendor.js')) return vendor
    if (name.endsWith('utils/wechatUpdate.js')) return load(resolve(root, 'mp-weixin/utils/wechatUpdate.js'))
    if (name.endsWith('stores/ledger.js')) return { useLedger: () => ({
      state: { token: 'test-only' }, restore: async () => { events.push('restore') }, refresh: async () => {},
    }) }
    if (name.endsWith('utils/api.js')) return { setAuthExpiredHandler() {} }
    throw new Error(`Unexpected dependency: ${name}`)
  }, module, module.exports, () => [{ route }])
  cache.set(path, module.exports)
  return module.exports
}
load(resolve(root, 'mp-weixin/app.js'))
appComponent.setup({})
await launch()
assert.deepEqual(events, ['manager', 'restore'])
assert.equal(typeof pageShow, 'function')
callbacks.check({ hasUpdate: true })
assert.equal(route, 'pages/required-update/required-update')
assert.equal(interceptors.get('navigateBack')({}), false)
callbacks.ready()
assert.equal(modals.length, 1)
assert.equal(modals[0].showCancel, false)
assert.equal(modals[0].confirmText, '重启更新')
show(); pageShow()
assert.equal(modals.length, 1)
modals[0].success({ confirm: true }); modals[0].complete()
assert.equal(applied, 1)
const pages = JSON.parse(readFileSync(resolve(root, 'mp-weixin/app.json'))).pages
assert.ok(pages.includes('pages/required-update/required-update'))
const assets = resolve(root, 'h5/assets')
for (const file of readdirSync(assets).filter(file => file.endsWith('.js'))) {
  assert.doesNotMatch(readFileSync(resolve(assets, file), 'utf8'), /getUpdateManager|需要更新后使用|新版本下载超时/, file)
}
console.log('PASS: 实际微信生产包启动时序、全局生命周期、导航拦截及重启接线；H5产物排除微信更新实现')
