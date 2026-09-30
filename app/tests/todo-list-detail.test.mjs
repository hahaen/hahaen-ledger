import { readFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'
const require = createRequire(import.meta.url)
const transpile = source => ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ESNext } }).outputText
const helpers = {}
new Function('exports', transpile(await readFile(new URL('../src/utils/todoDisplay.ts', import.meta.url), 'utf8')))(helpers)
async function page(name, api, fields) {
  const source = await readFile(new URL(`../src/pages/${name}/${name}.vue`, import.meta.url), 'utf8')
  const script = source.match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const exports = {}, hooks = {}, navigation = [], toasts = []
  const deps = { vue: require('vue'), '@dcloudio/uni-app': { onLoad: fn => { hooks.load = fn }, onShow: fn => { hooks.show = fn } }, '../../utils/api': { todoApi: api }, '../../utils/todoDisplay': helpers, '../../utils/wechatShare': { registerWechatShare() {} }, '../../stores/ledger': { useLedger: () => ({ state: { token: 'fixture' } }) } }
  new Function('require', 'exports', 'uni', 'getCurrentPages', transpile(script + `\nexport { ${fields} }`))(name => deps[name] || {}, exports, { navigateTo: v => navigation.push(v.url), redirectTo: v => navigation.push(v.url), navigateBack: () => navigation.push('back'), showToast: v => toasts.push(v.title) }, () => [])
  return { ...exports, hooks, navigation, toasts }
}
const pending = { id: '123', status: 'PENDING', title: '待办', dueAt: '2026-10-01T12:00:00' }
test('北京时间期限：不足一天、整天、逾期、显式时区及无效日期', () => {
  const now = Date.parse('2026-09-30T12:00:00+08:00')
  for (const [date, label] of [['2026-10-01T12:00:00','距离1天'],['2026-10-01T12:00:01','距离2天'],['2026-09-30T12:01:00','距离1天'],['2026-09-30T11:59:00','过期1天'],['2026-09-28T12:00:00','过期2天'],['2026-09-30T12:00:00','距离0天'],['2026-10-01T04:00:00Z','距离1天'],['invalid','时间待确认']]) assert.equal(helpers.todoDueLabel(date, now), label)
})
test('完成防重、失败保留状态与幂等键，成功刷新并进入详情', async () => {
  const calls = []; let reject; let fail = true
  const p = await page('ha-todo', { complete: (id, key) => { calls.push({ id, key }); return fail ? new Promise((_, no) => { reject = no }) : Promise.resolve() }, list: async () => ({ items: [], pendingCount: 0, completedCount: 1, hasMore: false }) }, 'complete, actingId, completedCount, openDetail')
  const item = { ...pending }; const first = p.complete(item); await p.complete(item); p.openDetail(item)
  assert.equal(calls.length, 1); assert.equal(p.navigation.length, 0)
  reject(new Error('网络失败')); await first; assert.equal(item.status, 'PENDING'); assert.equal(p.actingId.value, '')
  fail = false; await p.complete(item); assert.equal(calls[0].key, calls[1].key); assert.equal(item.status, 'COMPLETED'); assert.equal(p.completedCount.value, 1)
  await p.complete(item); assert.equal(calls.length, 2)
  p.openDetail(item); assert.equal(p.navigation[0], '/pages/ha-todo-detail/ha-todo-detail?id=123')
})
test('详情链接与加载重试，待完成可编辑，已完成不可编辑', async () => {
  let fail = true
  const p = await page('ha-todo-detail', { detail: async () => { if (fail) throw new Error('加载失败'); return { ...pending } } }, 'id, item, loadDetail, loadError, edit')
  p.hooks.load({ id: 'bad' }); assert.equal(p.loadError.value, '待办链接无效'); assert.equal(p.id.value, '')
  p.hooks.load({ id: '123' }); await p.loadDetail(); assert.equal(p.loadError.value, '加载失败')
  fail = false; await p.loadDetail(); p.edit(); assert.equal(p.navigation[0], '/pages/ha-todo-editor/ha-todo-editor?id=123')
  p.item.value.status = 'COMPLETED'; p.edit(); assert.equal(p.navigation.length, 1)
})
test('删除需确认、执行防重、失败保留弹层及同键重试，成功回清单', async () => {
  const calls = []; let reject; let fail = true
  const p = await page('ha-todo-detail', { remove: (id, key) => { calls.push({ id, key }); return fail ? new Promise((_, no) => { reject = no }) : Promise.resolve() } }, 'id, item, remove, deleteOpen, deleteError')
  p.id.value = '123'; p.item.value = { ...pending, status: 'COMPLETED' }; await p.remove(); assert.equal(calls.length, 0)
  p.deleteOpen.value = true; const first = p.remove(); await p.remove(); assert.equal(calls.length, 1)
  reject(new Error('删除失败')); await first; assert.equal(p.deleteOpen.value, true); assert.equal(p.deleteError.value, '删除失败')
  fail = false; await p.remove(); assert.equal(calls[0].key, calls[1].key); assert.equal(p.deleteOpen.value, false); assert.deepEqual(p.navigation, ['/pages/ha-todo/ha-todo'])
})

test('详情直接打开无页面栈时返回待办清单', async () => {
  const p = await page('ha-todo-detail', {}, 'back')
  p.back(); assert.deepEqual(p.navigation, ['/pages/ha-todo/ha-todo'])
})
