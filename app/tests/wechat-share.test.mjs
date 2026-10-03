import { readFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'

const require = createRequire(import.meta.url)
const uniRequire = createRequire(require.resolve('@dcloudio/uni-app'))
const shared = uniRequire('@dcloudio/uni-shared')
const shareSource = await readFile(new URL('../src/utils/wechatShare.ts', import.meta.url), 'utf8')
const runtimeSource = await readFile(uniRequire.resolve('@dcloudio/uni-mp-weixin/dist/uni.mp.esm.js'), 'utf8')
const runtimeHooks = runtimeSource.slice(runtimeSource.indexOf('function findHooks('), runtimeSource.indexOf('\nconst HOOKS ='))
const pages = JSON.parse(await readFile(new URL('../src/pages.json', import.meta.url), 'utf8')).pages
const transpile = source => ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS } }).outputText
const staticResourceSource = await readFile(new URL('../src/utils/staticResource.ts', import.meta.url), 'utf8')
const expectedImage = 'https://hahaen.xyz/minio-api/haji/wx/share-welcome.png'
function preprocess(source, platform) {
  return source.replace(/\/\/ #ifdef MP-WEIXIN\n([\s\S]*?)\/\/ #endif/g, (_, body) => platform === 'mp-weixin' ? body : '')
}
function moduleExports(source, dependencies = {}) {
  const exports = {}
  new Function('require', 'exports', transpile(source))(name => {
    if (!(name in dependencies)) throw new Error(`Unexpected dependency ${name}`)
    return dependencies[name]
  }, exports)
  return exports
}
function nativeMethods(options, getApp = () => undefined) {
  // 使用真实 uni-app 初始化与 once 语义；首次读不到 App 不能依赖之后补注册。
  return new Function('ON_READY', 'hasOwn', 'MINI_PROGRAM_PAGE_RUNTIME_HOOKS', 'once', 'isFunction', 'isArray', 'getApp', 'isUniLifecycleHook', '__VUE_OPTIONS_API__', `${runtimeHooks}\nconst methods = {}; initUnknownHooks(methods, arguments[9]); initRuntimeHooks(methods, arguments[9].__runtimeHooks); initMixinRuntimeHooks(methods); return methods;`)(
    'onReady', (object, key) => Object.hasOwn(object, key), shared.MINI_PROGRAM_PAGE_RUNTIME_HOOKS,
    shared.once, value => typeof value === 'function', Array.isArray, getApp, shared.isUniLifecycleHook, true, options,
  )
}
async function pageOptions(path, platform) {
  const source = await readFile(new URL(`../src/${path}.vue`, import.meta.url), 'utf8')
  const optionsScript = source.match(/<script lang="ts">([\s\S]*?)<\/script>/)?.[1] || 'export default {}'
  const share = moduleExports(preprocess(shareSource, platform), { './staticResource': moduleExports(staticResourceSource) })
  const result = moduleExports(preprocess(optionsScript, platform), {
    '../../utils/wechatShare': share, '../../../utils/wechatShare': share,
  })
  return result.default
}

for (const { path } of pages) {
  test(`${path} 在 App 尚未就绪时仍注册原生分享并返回固定内容`, async () => {
    const options = await pageOptions(path, 'mp-weixin')
    const methods = nativeMethods(options)
    assert.equal(typeof methods.onShareAppMessage, 'function', '分享方法不能依赖全局 App 实例')
    const result = methods.onShareAppMessage.call({ $vm: { $callHook: hook => options[hook]() } }, { from: 'menu' })
    assert.deepEqual(result, { title: '哈记账｜简单记账，安心生活', path: '/pages/index/index', imageUrl: expectedImage })
  })
}

test('H5 所有页面不注册微信分享选项或全局 mixin', async () => {
  for (const { path } of pages) {
    const options = await pageOptions(path, 'h5')
    assert.equal('onShareAppMessage' in options, false)
  }
  const main = await readFile(new URL('../src/main.ts', import.meta.url), 'utf8')
  assert.equal(preprocess(main, 'h5').includes('wechatShare'), false)
})

test('欢迎图备份保持5:4 PNG，小于200KB', async () => {
  const image = await readFile(new URL('../offloaded-static-assets/wx/share-welcome.png', import.meta.url))
  assert.equal(image.subarray(0, 8).toString('hex'), '89504e470d0a1a0a')
  assert.equal(image.readUInt32BE(16), 500)
  assert.equal(image.readUInt32BE(20), 400)
  assert.ok(image.length < 200 * 1024)
})

test('分享无需微信本地文件API，始终返回公开HTTPS固定图片', () => {
  // 未提供 uni/wx，分享函数也应直接返回完整配置，避免本地图片准备影响回调。
  const share = moduleExports(shareSource, { './staticResource': moduleExports(staticResourceSource) })
  for (let index = 0; index < 2; index++) {
    assert.deepEqual(share.createWechatShareMessage(), {
      title: '哈记账｜简单记账，安心生活', path: '/pages/index/index', imageUrl: expectedImage,
    })
  }
})
