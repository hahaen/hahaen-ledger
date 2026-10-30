import { readFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'
const require = createRequire(import.meta.url)
const vue = require('vue'), { parse, compileScript } = require('vue/compiler-sfc')
const { renderToString } = require('vue/server-renderer')
const transpile = source => ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ESNext } }).outputText
async function helpers(path) {
  const output = {}
  new Function('exports', transpile(await readFile(new URL(path, import.meta.url), 'utf8')))(output)
  return output
}
const lottery = await helpers('../src/utils/lottery.ts'), money = await helpers('../src/utils/money.ts')
async function component(path, dependencies) {
  const { descriptor } = parse(await readFile(new URL(path, import.meta.url), 'utf8'))
  const compiled = compileScript(descriptor, { id: 'lottery-draw-test', inlineTemplate: true })
  const output = {}
  new Function('require', 'exports', transpile(compiled.content))(name => {
    if (name === 'vue') return vue
    if (!(name in dependencies)) throw new Error(`未配置组件依赖：${name}`)
    return dependencies[name]
  }, output)
  return output.default
}
const MoneyDisplay = await component('../src/components/MoneyDisplay.vue', { '../utils/money': money })
const LotteryNumbers = await component('../src/components/LotteryNumbers.vue', {})
for (const game of ['DLT', 'SSQ']) test(`${game}开奖卡片渲染个人金额、零元、待确认和旧接口兼容`, async () => {
  const records = [
    { id: '1', personalSummary: { costCents: '800', prizeCents: '123456789', entryCount: 2, pendingCount: 0 } },
    { id: '2', personalSummary: { costCents: '0', prizeCents: '0', entryCount: 0, pendingCount: 0 } },
    { id: '3', personalSummary: { costCents: '600', prizeCents: null, entryCount: 1, pendingCount: 1 } },
    { id: '4' },
  ].map(item => ({ ...item, game, issue: game === 'DLT' ? '26123' : '2026124', red: '01,02,03,04,05', blue: '01', drawDate: '2026-10-30' }))
  const requests = []
  let loaded
  const Page = await component('../src/pages/lottery-draws/lottery-draws.vue', {
    '@dcloudio/uni-app': { onLoad: callback => { loaded = callback } },
    '../../utils/wechatShare': { createWechatShareMessage: () => ({}) },
    '../../utils/api': { lotteryApi: { draws: async (...args) => { requests.push(args); return { items: records, hasMore: true, sync: {} } } } },
    '../../utils/lottery': lottery,
    '../../components/MoneyDisplay.vue': { default: MoneyDisplay },
    '../../components/LotteryNumbers.vue': { default: LotteryNumbers },
    '../../components/NativeNavigation.vue': { default: { render: () => null } },
  })
  const app = vue.createSSRApp({ ...Page, async setup(...args) {
    const render = Page.setup(...args)
    loaded({ game })
    await vue.nextTick()
    return render
  } })
  const html = await renderToString(app)
  assert.deepEqual(requests, [[game, 1]])
  assert.match(html, /1,234,567\.89/)
  assert.equal((html.match(/待确认/g) || []).length, 1)
  assert.equal((html.match(/暂不可用/g) || []).length, 2)
  assert.match(html, /成本/); assert.match(html, /奖金/); assert.match(html, /加载更多/)
  assert.equal((html.match(/class="money-value">0</g) || []).length, 2)
  assert.match(html, /查看奖金与官方依据/)
})
