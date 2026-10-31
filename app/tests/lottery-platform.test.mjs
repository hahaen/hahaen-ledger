import { readFileSync } from 'node:fs'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'
import { parsePages, preprocessPlatform, preprocessSfc } from './helpers/platform.mjs'

const require = createRequire(import.meta.url)
const vue = require('vue')
const { parse, compileScript } = require('vue/compiler-sfc')
const { renderToString } = require('vue/server-renderer')
const read = path => readFileSync(new URL(`../src/${path}`, import.meta.url), 'utf8')
const routes = ['lottery', 'lottery-editor', 'lottery-detail', 'lottery-draws'].map(name => `pages/${name}/${name}`)

for (const platform of ['h5', 'mp-weixin']) {
  test(`${platform} 彩票路由、调用和样式遵守平台边界`, () => {
    const pages = parsePages(read('pages.json'), platform).pages.map(page => page.path)
    assert.deepEqual(pages.filter(path => routes.includes(path)), platform === 'h5' ? routes : [])
    assert.ok(pages.includes('pages/ha-todo/ha-todo'))
    assert.ok(pages.includes('pages/notification-center/notification-center'))
    for (const [path, kind, pattern] of [
      ['utils/api.ts', 'js', /export const lotteryApi/],
      ['styles.scss', 'css', /@use '\.\/lottery.scss'/],
      ['pages/mine/mine.vue', 'html', /openLottery|lotteryEnabled|彩票/],
    ]) {
      const source = path.endsWith('.vue') ? preprocessSfc(read(path), platform) : preprocessPlatform(read(path), platform, kind)
      if (platform === 'h5') assert.match(source, pattern)
      else assert.doesNotMatch(source, pattern)
    }
  })

  for (const enabled of [true, false]) test(`${platform} 我的入口在彩票权限=${enabled}时的真实模板渲染`, async () => {
    const source = preprocessSfc(read('pages/mine/mine.vue'), platform)
    const { descriptor } = parse(source)
    const compiled = compileScript(descriptor, { id: 'mine-platform', inlineTemplate: true })
    const dependencies = {
      vue,
      '@dcloudio/uni-app': { onShow() {} },
      '../../utils/wechatShare': { createWechatShareMessage() {} },
      '../../stores/ledger': { useLedger: () => ({ state: { token: 'fixture-session' } }) },
      '../../utils/staticResource': { staticResource: path => path },
      '../../utils/file': { currentAvatar: async () => null },
      '../../utils/api': { request: async () => ({ nickname: '测试用户', lotteryEnabled: enabled }) },
      '../../components/BottomNav.vue': { default: { render: () => null } },
      '../../components/PageHeader.vue': { default: { render: () => null } },
    }
    const exports = {}
    // 使用实际页面加载逻辑与模板；仅替换资料请求和平台依赖。
    // 在 setup 的所有变量初始化后加载资料，避免异步生命周期与 SSR 时序耦合。
    const setupCode = compiled.content.replace('\nreturn (_ctx', '\nawait loadProfile();\nreturn (_ctx')
      .replace(/setup\(__props\)/, 'async setup(__props)')
    assert.notEqual(setupCode, compiled.content)
    new Function('require', 'exports', ts.transpileModule(setupCode, {
      compilerOptions: { target: ts.ScriptTarget.ESNext, module: ts.ModuleKind.CommonJS },
    }).outputText)(name => {
      assert.ok(name in dependencies, `缺少依赖 ${name}`)
      return dependencies[name]
    }, exports)
    const html = await renderToString(vue.createSSRApp(exports.default))
    assert.equal(html.includes('aria-label="彩票"'), platform === 'h5' && enabled)
    assert.match(html, /待办清单/)
    assert.match(html, /通知中心/)
  })
}
