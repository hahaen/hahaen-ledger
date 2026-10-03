// 先执行 build:mp-weixin；直接检查编译后的页面选项，不以源码命中代替产物验证。
import { readFileSync } from 'node:fs'
import { createRequire } from 'node:module'
import { dirname, resolve } from 'node:path'
import assert from 'node:assert/strict'

const require = createRequire(import.meta.url)
const uniRequire = createRequire(require.resolve('@dcloudio/uni-app'))
const shared = uniRequire('@dcloudio/uni-shared')
const runtime = readFileSync(uniRequire.resolve('@dcloudio/uni-mp-weixin/dist/uni.mp.esm.js'), 'utf8')
const hooks = runtime.slice(runtime.indexOf('function findHooks('), runtime.indexOf('\nconst HOOKS ='))
const buildRoot = resolve(import.meta.dirname, '../dist/build/mp-weixin')
const pages = JSON.parse(readFileSync(resolve(buildRoot, 'app.json'), 'utf8')).pages
const vendor = {
  defineComponent: value => value,
  _export_sfc: (value, properties) => Object.assign(value, Object.fromEntries(properties)),
  index: { getFileSystemManager: () => ({ copyFileSync(source, destination) {
    assert.equal(source, '/static/share-welcome.png')
    assert.equal(destination, 'wxfile://usr/share-welcome.png')
  } }) },
  wx$1: { env: { USER_DATA_PATH: 'wxfile://usr' } },
}
const cache = new Map()
function loadShare(path) {
  if (cache.has(path)) return cache.get(path)
  const module = { exports: {} }
  new Function('require', 'module', 'exports', readFileSync(path, 'utf8'))(
    name => { assert.ok(name.endsWith('common/vendor.js')); return vendor }, module, module.exports,
  )
  cache.set(path, module.exports)
  return module.exports
}
for (const page of pages) {
  const path = resolve(buildRoot, `${page}.js`)
  let options
  const module = { exports: {} }
  new Function('require', 'module', 'exports', 'wx', readFileSync(path, 'utf8'))(name => {
    if (name.endsWith('common/vendor.js')) return vendor
    if (name.endsWith('utils/wechatShare.js')) return loadShare(resolve(dirname(path), name))
    return {} // 业务 setup 不执行；仅捕获实际生产包交给微信的页面选项。
  }, module, module.exports, { createPage(value) { options = value } })
  assert.ok(options, `${page} 没有注册页面`)
  const methods = new Function('ON_READY', 'hasOwn', 'MINI_PROGRAM_PAGE_RUNTIME_HOOKS', 'once', 'isFunction', 'isArray', 'getApp', 'isUniLifecycleHook', '__VUE_OPTIONS_API__', `${hooks}\nconst methods = {}; initUnknownHooks(methods, arguments[9]); initRuntimeHooks(methods, arguments[9].__runtimeHooks); initMixinRuntimeHooks(methods); return methods;`)(
    'onReady', (object, key) => Object.hasOwn(object, key), shared.MINI_PROGRAM_PAGE_RUNTIME_HOOKS,
    shared.once, value => typeof value === 'function', Array.isArray, () => undefined, shared.isUniLifecycleHook, true, options,
  )
  assert.equal(typeof methods.onShareAppMessage, 'function', `${page} 在 App 未就绪时缺少原生分享方法`)
  const result = methods.onShareAppMessage.call({ $vm: { $callHook: hook => options[hook]() } }, { from: 'menu' })
  assert.deepEqual(result, { title: '哈记账｜简单记账，安心生活', path: '/pages/index/index', imageUrl: 'wxfile://usr/share-welcome.png' }, page)
}
assert.deepEqual(readFileSync(resolve(buildRoot, 'static/share-welcome.png')), readFileSync(resolve(import.meta.dirname, '../src/static/share-welcome.png')))
console.log(`PASS: ${pages.length}/${pages.length} 个生产页面在 App 未就绪时仍返回固定分享内容；打包 PNG 内容一致`)
