import { readFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'
const require = createRequire(import.meta.url), vue = require('vue')
const { compile } = createRequire(require.resolve('vue'))('@vue/compiler-dom')
const transpile = source => ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ESNext } }).outputText
const deferred = () => { let resolve, reject; const promise = new Promise((yes, no) => { resolve = yes; reject = no }); return { promise, resolve, reject } }
const date = '2026-10-01', month = '2026-10'
const transaction = { id: 't1', type: 'EXPENSE', amountCents: 100, occurredAt: date + 'T10:00:00' }
const summary = { month, dailyExpenseCents: 100, expenseCents: 100, incomeCents: 0, transactions: [transaction] }
const account = { id: 'a1', kind: 'FUND', name: '资金', status: 'ACTIVE', sortOrder: 0, includedInNetAsset: true, balanceCents: 100 }
const asset = { totalAssetsCents: 100, totalLiabilitiesCents: 0, netAssetsCents: 100, accounts: [account] }
const item = { id: 'i1', name: '物品', status: 'ACTIVE', purchasedOn: date, netCostCents: 100, dailyCostCents: 100, serviceDays: 1 }
const itemOverview = { totalAssetsCents: 100, totalDailyCostCents: 100, activeCount: 1, retiredCount: 0, items: [item], page: 1, hasMore: false }
const calendar = { month, days: [{ date, day: 1, currentMonth: true, today: true, hasRecords: true }] }
const day = { date, expenseCents: 100, incomeCents: 0, balanceCents: -100, transactions: [transaction] }
async function page(name) {
  const source = await readFile(new URL(`../src/pages/${name}/${name}.vue`, import.meta.url), 'utf8')
  const script = source.match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const ast = ts.createSourceFile('page.ts', script, ts.ScriptTarget.Latest, true, ts.ScriptKind.TS)
  const names = ast.statements.flatMap(s => ts.isVariableStatement(s) ? s.declarationList.declarations.filter(d => ts.isIdentifier(d.name)).map(d => d.name.text) : ts.isFunctionDeclaration(s) ? [s.name.text] : [])
  const state = vue.reactive({ token: 'fixture-one', accounts: [], summary: undefined })
  const api = { request: async url => url.startsWith('/api/app/calendar?') ? calendar : url.startsWith('/api/app/calendar/') ? day : asset, refresh: async () => summary, recent: async () => ({ transactions: [transaction], startMonth: month, hasMore: false }), list: async () => itemOverview }
  const ledger = { state, refresh: (...args) => api.refresh(...args), loadHomeRecentTransactions: (...args) => api.recent(...args), login: async () => {} }
  const hooks = {}, exports = {}, toasts = []
  const deps = { vue, '@dcloudio/uni-app': { onShow: fn => { hooks.show = fn }, onPullDownRefresh: fn => { hooks.pull = fn }, onReachBottom: fn => { hooks.bottom = fn } }, '../../stores/ledger': { useLedger: () => ledger }, '../../utils/api': { request: (...args) => api.request(...args), itemApi: { list: (...args) => api.list(...args) } }, '../../utils/money': { localDateTime: () => date + 'T12:00:00' }, '../../utils/staticResource': { staticResource: value => '/fixture/' + value }, '../../utils/wechatShare': { registerWechatShare() {} } }
  new Function('require', 'exports', 'uni', transpile(script + `\nexport { ${names.join(',')} };`))(name => deps[name] || {}, exports, { showToast: v => toasts.push(v.title), stopPullDownRefresh() {}, reLaunch() {}, navigateTo() {} })
  const render = new Function('Vue', transpile(compile(source.match(/<template>([\s\S]*?)<\/template>\s*$/)[1], { isCustomElement: () => true, expressionPlugins: ['typescript'] }).code))(vue)
  const snapshot = () => {
    const walk = node => typeof node === 'string' ? node : ([typeof node?.type === 'string' ? node.type : '', Object.fromEntries(Object.entries(node?.props || {}).filter(([k]) => !k.startsWith('on'))), Array.isArray(node?.children) ? node.children.map(walk) : typeof node?.children === 'string' ? node.children : ''])
    return JSON.stringify(walk(render(vue.proxyRefs({ ...exports, staticResource: deps['../../utils/staticResource'].staticResource, uni: { navigateTo() {} } }), [])))
  }
  if (name === 'calendar') exports.cursor.value = new Date(date + 'T00:00:00')
  return { ...exports, state, api, hooks, toasts, snapshot, run: () => name === 'calendar' ? exports.loadMonth() : exports.load() }
}
for (const name of ['index', 'calendar', 'assets', 'items']) {
  test(`${name}：慢请求切回保留完整渲染，结果返回后更新`, async () => {
    const p = await page(name); await p.run(); const before = p.snapshot(); const pending = deferred()
    if (name === 'index') p.api.recent = () => pending.promise
    else if (name === 'calendar') p.api.request = url => url.includes('?') ? pending.promise : Promise.resolve(day)
    else if (name === 'assets') p.api.request = () => pending.promise
    else p.api.list = () => pending.promise
    const refresh = p.hooks.show(); await Promise.resolve(); assert.equal(p.snapshot(), before)
    const next = name === 'index' ? { transactions: [{ ...transaction, id: 't2' }], startMonth: month, hasMore: false } : name === 'calendar' ? { ...calendar, days: [{ ...calendar.days[0], day: 2 }] } : name === 'assets' ? { ...asset, netAssetsCents: 200 } : { ...itemOverview, items: [{ ...item, name: '新物品' }] }
    pending.resolve(next); await refresh; await new Promise(resolve => setImmediate(resolve)); assert.notEqual(p.snapshot(), before)
  })
  test(`${name}：后台失败保留旧数据并明确反馈，首次失败可重试`, async () => {
    const p = await page(name); await p.run(); const before = p.snapshot()
    if (name === 'index') p.api.recent = async () => { throw new Error('offline') }
    else if (name === 'items') p.api.list = async () => { throw new Error('offline') }
    else p.api.request = async () => { throw new Error('offline') }
    await p.run(); assert.equal(p.snapshot(), before); assert.ok(p.toasts.length)
    const first = await page(name)
    if (name === 'index') first.api.refresh = first.api.recent = async () => { throw new Error('offline') }
    else if (name === 'items') first.api.list = async () => { throw new Error('offline') }
    else first.api.request = async () => { throw new Error('offline') }
    await first.run(); assert.match(first.snapshot(), /暂不可用|暂时无法加载/)
  })
  test(`${name}：会话清理且旧响应不能恢复原用户数据`, async () => {
    const p = await page(name); await p.run(); const pending = deferred()
    if (name === 'index') p.api.recent = () => pending.promise
    else if (name === 'items') p.api.list = () => pending.promise
    else p.api.request = () => pending.promise
    const refresh = p.run(); p.state.token = ''; const cleared = p.snapshot()
    assert.doesNotMatch(cleared, /"t1"|"a1"|"i1"/)
    pending.resolve(name === 'index' ? { transactions: [transaction], startMonth: month } : name === 'items' ? itemOverview : name === 'assets' ? asset : calendar)
    await refresh; assert.equal(p.snapshot(), cleared)
  })
}
for (const name of ['index', 'calendar', 'assets', 'items']) {
  test(`${name}：已加载空状态切回不会闪成加载提示`, async () => {
    const p = await page(name)
    if (name === 'index') p.api.recent = async () => ({ transactions: [], startMonth: month, hasMore: false })
    else if (name === 'calendar') p.api.request = async url => url.includes('?') ? calendar : { ...day, transactions: [] }
    else if (name === 'assets') p.api.request = async () => ({ ...asset, accounts: [] })
    else p.api.list = async () => ({ ...itemOverview, items: [], activeCount: 0 })
    await p.run(); const before = p.snapshot(); const pending = deferred()
    if (name === 'index') p.api.recent = () => pending.promise
    else if (name === 'items') p.api.list = () => pending.promise
    else p.api.request = () => pending.promise
    const refresh = p.run(); assert.equal(p.snapshot(), before)
    pending.reject(new Error('offline')); await refresh; assert.equal(p.snapshot(), before)
  })
}
test('日历：换月/换日清除不同日期数据，快速切换旧响应不能覆盖', async () => {
  const p = await page('calendar'); await p.run(); const pending = deferred(); p.api.request = () => pending.promise
  p.cursor.value = new Date('2026-09-01T00:00:00'); const previous = p.loadMonth()
  assert.match(p.snapshot(), /正在加载月历/); assert.equal(p.detail.value, null)
  p.cursor.value = new Date(date + 'T00:00:00'); p.api.request = async url => url.includes('?') ? calendar : day
  await p.loadMonth(); pending.resolve({ month: '2026-09', days: [] }); await previous
  assert.equal(p.loadedMonth.value, month); assert.equal(p.detail.value.date, date)
  const pendingDay = deferred(); p.api.request = () => pendingDay.promise; p.selected.value = '2026-10-02'
  const second = p.loadDay(p.selected.value); assert.equal(p.detail.value, null); assert.match(p.snapshot(), /正在加载当天账单/)
  pendingDay.resolve({ ...day, date: '2026-10-02', transactions: [] }); await second; assert.equal(p.detail.value.date, '2026-10-02')
})
test('日历：相同日期详情刷新失败保留汇总与账单', async () => {
  const p = await page('calendar'); await p.run(); const before = p.snapshot()
  p.api.request = async () => { throw new Error('offline') }; await p.loadDay(date)
  assert.equal(p.snapshot(), before); assert.ok(p.toasts.length)
})
test('物品：切换筛选有加载反馈，快速切换及后台请求不覆盖当前筛选', async () => {
  const p = await page('items'); await p.run(); const old = deferred(); p.api.list = () => old.promise
  const refresh = p.load(); const filtered = deferred(); p.api.list = () => filtered.promise; p.select('RETIRED')
  assert.equal(p.items.value.length, 0); assert.match(p.snapshot(), /正在加载物品/)
  old.resolve(itemOverview); await refresh; assert.equal(p.items.value.length, 0)
  filtered.resolve({ ...itemOverview, items: [{ ...item, status: 'RETIRED' }] }); await new Promise(resolve => setImmediate(resolve))
  assert.equal(p.loadedFilter.value, 'RETIRED'); assert.equal(p.items.value[0].status, 'RETIRED')
})
test('首页：首次反馈和主动下拉指示保留，同月后台刷新不触发下拉指示', async () => {
  const p = await page('index'); const pending = deferred(); p.api.recent = () => pending.promise
  const first = p.load(); assert.match(p.snapshot(), /正在加载收支|正在加载账单/)
  pending.resolve({ transactions: [], startMonth: month, hasMore: false }); await first
  p.api.recent = async () => ({ transactions: [], startMonth: month, hasMore: false })
  const pull = p.hooks.pull(); assert.equal(p.refreshing.value, true); await pull; assert.equal(p.refreshing.value, false)
  p.summary.value = { ...summary, month: '2026-09' }; const slow = deferred(); p.api.refresh = () => slow.promise
  const nextMonth = p.load(); assert.match(p.snapshot(), /正在加载收支/)
  slow.resolve(summary); await nextMonth; assert.equal(p.summary.value.month, month)
})
for (const name of ['assets', 'items']) {
  test(`${name}：先发后到的请求不能覆盖最新数据`, async () => {
    const p = await page(name); const old = deferred()
    if (name === 'assets') p.api.request = () => old.promise; else p.api.list = () => old.promise
    const first = p.run()
    if (name === 'assets') p.api.request = async () => ({ ...asset, netAssetsCents: 200 }); else p.api.list = async () => ({ ...itemOverview, items: [{ ...item, name: '新物品' }] })
    await p.run(); const latest = p.snapshot(); old.resolve(name === 'assets' ? asset : itemOverview); await first
    assert.equal(p.snapshot(), latest)
  })
}
