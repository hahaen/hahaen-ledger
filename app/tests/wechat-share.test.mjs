import { readFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'

const require = createRequire(import.meta.url)
const uniRequire = createRequire(require.resolve('@dcloudio/uni-app'))
const shared = uniRequire('@dcloudio/uni-shared')
const mainSource = await readFile(new URL('../src/main.ts', import.meta.url), 'utf8')
const staticSource = await readFile(new URL('../src/utils/staticResource.ts', import.meta.url), 'utf8')
const shareSource = await readFile(new URL('../src/utils/wechatShare.ts', import.meta.url), 'utf8')
const runtimeSource = await readFile(uniRequire.resolve('@dcloudio/uni-mp-weixin/dist/uni.mp.esm.js'), 'utf8')
const runtimeHooks = runtimeSource.slice(runtimeSource.indexOf('function initHook('), runtimeSource.indexOf('\nconst HOOKS ='))
const transpile = source => ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS } }).outputText
function moduleExports(source, dependencies) {
  const exports = {}
  new Function('require', 'exports', transpile(source))(name => {
    if (!(name in dependencies)) throw new Error(`Unexpected dependency ${name}`)
    return dependencies[name]
  }, exports)
  return exports
}
function preprocess(source, platform) {
  return source.replace(/\/\/ #ifdef MP-WEIXIN\n([\s\S]*?)\/\/ #endif/g, (_, body) => platform === 'mp-weixin' ? body : '')
}
function createApp(platform) {
  const mixins = []
  const app = { mixin(value) { mixins.push(value); return this } }
  const share = moduleExports(preprocess(shareSource, platform), {
    './staticResource': moduleExports(staticSource, {}),
  })
  const main = moduleExports(preprocess(mainSource, platform), {
    vue: { createSSRApp: () => app }, './App.vue': {}, './styles.scss': {}, './utils/wechatShare': share,
  })
  main.createApp()
  return mixins
}

test('全局分享经 uni-app 真实运行时注册微信原生转发方法', () => {
  const mixins = createApp('mp-weixin')
  const methods = {}
  new Function('ON_READY', 'hasOwn', 'MINI_PROGRAM_PAGE_RUNTIME_HOOKS', 'once', 'isFunction', 'isArray', 'getApp', `${runtimeHooks}\ninitMixinRuntimeHooks(arguments[7]);`)(
    'onReady', (object, key) => Object.hasOwn(object, key), shared.MINI_PROGRAM_PAGE_RUNTIME_HOOKS,
    fn => fn, value => typeof value === 'function', Array.isArray,
    () => ({ $vm: { $: { appContext: { mixins } } } }), methods,
  )
  assert.equal(typeof methods.onShareAppMessage, 'function', '微信原生页面缺少 onShareAppMessage，菜单会不可转发')
  const result = methods.onShareAppMessage.call({ $vm: { $callHook: hook => mixins.find(mixin => hook in mixin)[hook]() } }, { from: 'menu' })
  assert.deepEqual(result, { title: '哈记账｜简单记账，安心生活', path: '/pages/index/index', imageUrl: 'https://hahaen.xyz/minio-api/haji/wx/brand.png' })
  assert.ok(result.imageUrl, '必须使用固定图片，不能默认截取个人账单')
})

test('H5 不注册微信分享生命周期', () => {
  assert.equal(createApp('h5').length, 0)
})
