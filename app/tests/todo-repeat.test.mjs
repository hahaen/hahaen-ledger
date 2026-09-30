import { readFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'
const require = createRequire(import.meta.url)
const transpile = source => ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ESNext } }).outputText
const helpers = {}
new Function('exports', transpile(await readFile(new URL('../src/utils/todoRepeat.ts', import.meta.url), 'utf8')))(helpers)
async function editor(api, previousPages = [], onBack = () => {}) {
  const file = await readFile(new URL('../src/pages/ha-todo-editor/ha-todo-editor.vue', import.meta.url), 'utf8')
  const script = file.match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const exposed = {}; const hooks = {}; const toasts = []; const navigation = []
  const dependencies = { vue: require('vue'), '@dcloudio/uni-app': { onLoad: cb => { hooks.load = cb }, onBackPress() {} }, '../../utils/api': { todoApi: api }, '../../utils/todoRepeat': helpers, '../../stores/ledger': { useLedger: () => ({ state: { token: 'fixture' } }) } }
  new Function('require', 'exports', 'uni', 'getCurrentPages', transpile(script + '\nexport { title, remind, customRepeat, save, snapshot, initialValue, loadDetail, editId, back, discardOpen, pickerMode }'))(name => dependencies[name] || {}, exposed, { showToast: v => toasts.push(v.title), redirectTo: v => navigation.push(v.url), navigateBack: options => { navigation.push('back'); onBack(options) } }, () => previousPages)
  hooks.load({})
  return { ...exposed, hooks, toasts, navigation }
}
test('切换模式清除隐藏选择，多选切换与校验', () => {
  const fields = { repeatMode: 'AFTER_COMPLETION', repeatUnit: 'MONTH', repeatInterval: 3, weekDays: '1,5', monthDays: '31', yearDays: '02-29', lastDay: true, fixedDates: '2027-01-01' }
  const cleaned = helpers.cleanRepeat(fields)
  assert.equal(cleaned.monthDays, null); assert.equal(cleaned.lastDay, false); assert.equal(cleaned.fixedDates, undefined)
  assert.deepEqual(helpers.cleanRepeat({ ...fields, repeatMode: 'FIXED_DATES' }), { repeatMode: 'FIXED_DATES', fixedDates: '2027-01-01' })
  assert.equal(helpers.toggleSelection('1,5', '1', true), '5'); assert.equal(helpers.toggleSelection('1,5', '3', true), '1,3,5')
  assert.ok(helpers.repeatError({ repeatMode: 'TIME', repeatUnit: 'WEEK', repeatInterval: 1 }))
  assert.ok(helpers.repeatError({ repeatMode: 'AFTER_COMPLETION', repeatUnit: 'DAY', repeatInterval: 366 }))
  assert.equal(helpers.repeatError({ repeatMode: 'TIME', repeatUnit: 'MONTH', repeatInterval: 2, lastDay: true }), '')
  assert.equal(helpers.recurrenceLabel({ recurrence: 'CUSTOM', anchorAt: '2027-01-01', repeatMode: 'AFTER_COMPLETION', repeatUnit: 'MONTH', repeatInterval: 2 }), '完成后每2月')
})
test('新增校验、失败保留幂等键及成功返回', async () => {
  const calls = []; let fail = true
  const page = await editor({ create: async body => { calls.push(body); if (fail) throw new Error('暂时失败') } })
  page.title.value = '周计划'
  page.customRepeat.value = { repeatMode: 'TIME', repeatUnit: 'WEEK', repeatInterval: 2, weekDays: '' }
  await page.save(); assert.equal(calls.length, 0)
  page.customRepeat.value.weekDays = '1,5'
  await page.save(); fail = false; await page.save()
  assert.equal(calls.length, 2); assert.equal(calls[0].idempotencyKey, calls[1].idempotencyKey)
  assert.equal(calls[1].weekDays, '1,5'); assert.equal(calls[1].recurrence, 'CUSTOM'); assert.equal(calls[1].remind, true); assert.deepEqual(page.navigation, ['/pages/ha-todo/ha-todo'])
})
test('编辑回填、规则未保存状态和月末提交', async () => {
  const calls = []
  const item = { title: '月末维护', note: '', recurrence: 'CUSTOM', monthInterval: 1, dueAt: '2090-01-31T09:00:00', remind: false, repeatMode: 'TIME', repeatUnit: 'MONTH', repeatInterval: 3, monthDays: '15,31', lastDay: true }
  const page = await editor({ detail: async () => item, edit: async (id, body) => calls.push({ id, body }) })
  page.editId.value = '123'; await page.loadDetail()
  assert.equal(page.customRepeat.value.monthDays, '15,31'); assert.equal(page.customRepeat.value.lastDay, true)
  assert.equal(page.snapshot(), page.initialValue.value); assert.equal(page.remind.value, false)
  page.back(); assert.equal(page.discardOpen.value, false)
  page.navigation.length = 0
  page.customRepeat.value.repeatInterval = 2; assert.notEqual(page.snapshot(), page.initialValue.value)
  page.back(); assert.equal(page.discardOpen.value, true); assert.equal(page.navigation.length, 0)
  await page.save(); assert.equal(calls[0].body.remind, false); assert.equal(calls[0].id, '123'); assert.equal(calls[0].body.lastDay, true); assert.equal(calls[0].body.repeatInterval, 2)
})

test('新增默认按时间每一天并开启提醒，填写后直接返回', async () => {
  const page = await editor({})
  assert.equal(page.remind.value, true)
  assert.deepEqual([page.customRepeat.value.repeatMode, page.customRepeat.value.repeatUnit, page.customRepeat.value.repeatInterval], ['TIME', 'DAY', 1])
  page.title.value = '未保存的新待办'
  page.pickerMode.value = 'date'; page.back()
  assert.equal(page.pickerMode.value, ''); assert.equal(page.navigation.length, 0)
  page.back(); assert.equal(page.discardOpen.value, false)
  assert.deepEqual(page.navigation, ['/pages/ha-todo/ha-todo'])
})
test('历史一次性待办编辑后按必选自定义规则保存', async () => {
  const calls = []
  const page = await editor({ detail: async () => ({ title: '旧待办', dueAt: '2090-01-01T09:00:00', recurrence: 'ONCE', remind: false }), edit: async (id, body) => calls.push(body) })
  page.editId.value = '456'; await page.loadDetail(); await page.save()
  assert.equal(calls[0].recurrence, 'CUSTOM'); assert.equal(calls[0].repeatMode, 'TIME')
  assert.equal(calls[0].repeatInterval, 1); assert.equal(calls[0].remind, false)
})

test('修改规则替换发生项后返回清单，不刷新失效详情', async () => {
  const stack = [{ route: 'pages/ha-todo/ha-todo' }, { route: 'pages/ha-todo-detail/ha-todo-detail' }, { route: 'pages/ha-todo-editor/ha-todo-editor' }]
  let deleted = false; let error = ''; let listRefreshes = 0
  const api = { detail: async () => {
    if (deleted) throw new Error('待办不存在，请刷新')
    return { title: '原待办', dueAt: '2090-01-01T09:00:00', recurrence: 'CUSTOM', repeatMode: 'TIME', repeatUnit: 'DAY', repeatInterval: 1 }
  }, edit: async () => { deleted = true } }
  const page = await editor(api, stack, options => {
    stack.splice(-options.delta)
    if (stack.at(-1)?.route === 'pages/ha-todo-detail/ha-todo-detail') {
      void api.detail().catch(e => { error = e.message })
    } else if (stack.at(-1)?.route === 'pages/ha-todo/ha-todo') listRefreshes++
  })
  page.editId.value = '123'; await page.loadDetail()
  page.customRepeat.value.repeatInterval = 2
  await page.save()
  assert.equal(error, '')
  assert.equal(listRefreshes, 1)
  assert.equal(stack.at(-1).route, 'pages/ha-todo/ha-todo')
})

test('详情直接进入编辑无清单栈时，保存重定向清单；取消仍回详情', async () => {
  const pages = [{ route: 'pages/ha-todo-detail/ha-todo-detail' }, { route: 'pages/ha-todo-editor/ha-todo-editor' }]
  const api = { detail: async () => ({ title: '待办', dueAt: '2090-01-01T09:00:00', recurrence: 'ONCE', remind: false }), edit: async () => {} }
  const page = await editor(api, pages)
  page.editId.value = '123'; await page.loadDetail(); page.back()
  assert.deepEqual(page.navigation, ['back'])
  page.navigation.length = 0; await page.save()
  assert.deepEqual(page.navigation, ['/pages/ha-todo/ha-todo'])
})
test('清单直接编辑成功退一页，失败留在编辑页且同键重试、防重', async () => {
  const calls = []; let reject; let fail = true
  const page = await editor({ detail: async () => ({ title: '待办', dueAt: '2090-01-01T09:00:00', recurrence: 'ONCE', remind: false }),
    edit: (id, body) => { calls.push(body); return fail ? new Promise((_, no) => { reject = no }) : Promise.resolve() }
  }, [{ route: 'pages/ha-todo/ha-todo' }, { route: 'pages/ha-todo-editor/ha-todo-editor' }], options => assert.equal(options.delta, 1))
  page.editId.value = '123'; await page.loadDetail()
  const first = page.save(); await page.save(); assert.equal(calls.length, 1)
  reject(new Error('暂时失败')); await first; assert.deepEqual(page.navigation, [])
  fail = false; await page.save(); assert.equal(calls[0].idempotencyKey, calls[1].idempotencyKey)
  assert.deepEqual(page.navigation, ['back'])
})
