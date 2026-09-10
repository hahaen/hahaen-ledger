import { readFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'

const require = createRequire(import.meta.url)
const transpile = source => ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ESNext, module: ts.ModuleKind.CommonJS } }).outputText
const defaultUni = { getStorageSync: () => '', setStorageSync() {}, showToast() {}, navigateBack() {}, switchTab() {} }
const run = (source, dependencies = {}, uni = defaultUni) => {
  const exports = {}
  new Function('require', 'exports', 'uni', 'getCurrentPages', 'setTimeout', transpile(source))(
    name => dependencies[name] || require(name), exports,
    uni, () => [], callback => { callback(); return 0 })
  return exports
}
const money = run(await readFile(new URL('../src/utils/money.ts', import.meta.url), 'utf8'))
const entryUtils = run(await readFile(new URL('../src/utils/entry.ts', import.meta.url), 'utf8'), { './money': money })
const deferred = () => { let resolve; let reject; const promise = new Promise((yes, no) => { resolve = yes; reject = no }); return { promise, resolve, reject } }
async function entryPage(ledger, request = async () => ({})) {
  const hooks = {}
  const events = { toasts: [], tabSwitches: [] }
  const uni = { ...defaultUni, showToast: options => events.toasts.push(options), switchTab: options => events.tabSwitches.push(options) }
  const source = (await readFile(new URL('../src/pages/entry/entry.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { initialize, save, setType, back, closeDiscard, confirmDiscard, openModal, selectAccount, confirmModal, formatPickerDate, isSameAccountSelection, modal, discardOpen, draftDate, draftTime, draftNote, draftAccountIndex, accountIndex, toIndex, amount, expression, note, idempotencyKey, loading, loadError, editingId, routeId, dateTime, saving, type, typeLabel, isEdit };', {
    '@dcloudio/uni-app': { onLoad: fn => { hooks.load = fn }, onBackPress() {} },
    '../../components/CenterModal.vue': {}, '../../components/EntryDateTimePicker.vue': {}, '../../components/MoneyDisplay.vue': {}, '../../stores/ledger': { useLedger: () => ledger },
    '../../utils/api': { request }, '../../utils/money': money, '../../utils/entry': entryUtils,
    '../../utils/id': { stringId: value => value == null ? '' : String(value) },
  }, uni)
  return { ...page, hooks, events }
}
const account = { id: '10', kind: 'FUND', status: 'ACTIVE', name: '测试账户' }

test('编辑记账沿用原账单类型且不能切换顶部类型', async () => {
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {} }, async () => ({
    refundedCents: 0,
    transaction: { type: 'INCOME', originalAmountCents: 1234, accountId: '10', occurredAt: '2026-09-09T12:30:00', note: '工资' },
  }))
  page.routeId.value = '20'
  page.editingId.value = '20'
  await page.initialize()
  assert.equal(page.isEdit.value, true)
  assert.equal(page.type.value, 'INCOME')
  assert.equal(page.typeLabel.value, '收入')
  page.setType('EXPENSE')
  assert.equal(page.type.value, 'INCOME')
})

test('编辑页放弃修改使用自定义确认弹窗', async () => {
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {} })
  await page.initialize()
  page.note.value = '未保存的修改'
  page.back()
  assert.equal(page.discardOpen.value, true)
  page.closeDiscard()
  assert.equal(page.discardOpen.value, false)
  page.back()
  page.confirmDiscard()
  assert.equal(page.discardOpen.value, false)
})

test('加载失败的编辑页不允许降级为新增提交', async () => {
  let writes = 0
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => { throw new Error('网络失败') }, createTransaction: async () => { writes++ } })
  page.routeId.value = '20'; page.editingId.value = '20'
  await page.initialize(); page.amount.value = '1000'
  await page.save()
  assert.equal(writes, 0)
  assert.equal(page.loadError.value, '网络失败')
})
test('保存期间与成功返回前连续点击只写入一次', async () => {
  const pending = deferred(); const payloads = []
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {}, createTransaction: async payload => { payloads.push(payload); await pending.promise } })
  await page.initialize(); page.expression.value = '1000.01'
  const saving = page.save('home'); await page.save('home')
  assert.equal(payloads.length, 1); assert.equal(payloads[0].amountCents, 100001)
  pending.resolve(); await saving; await page.save('home')
  assert.equal(payloads.length, 1)
})
test('写入失败后保留金额并使用原幂等键重试', async () => {
  const payloads = []
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {}, createTransaction: async payload => { payloads.push(payload); throw new Error('超时') } })
  await page.initialize(); page.expression.value = '8500'
  await page.save('home'); await page.save('home')
  assert.equal(page.amount.value, '8500'); assert.equal(page.saving.value, false)
  assert.equal(payloads.length, 2); assert.equal(payloads[0].idempotencyKey, payloads[1].idempotencyKey)
})
test('点击账户后立即应用选择并关闭账户弹窗', async () => {
  const secondAccount = { ...account, id: '11', name: '备用账户' }
  const page = await entryPage({ state: { accounts: [account, secondAccount] }, refresh: async () => {} })
  await page.initialize()
  page.openModal('account'); page.selectAccount(1)
  assert.equal(page.accountIndex.value, 1); assert.equal(page.modal.value, '')
  page.openModal('to'); page.selectAccount(0)
  assert.equal(page.toIndex.value, 0); assert.equal(page.modal.value, '')
})
test('转账的转出和转入账户不能选择同一账户', async () => {
  const secondAccount = { ...account, id: '11', name: '备用账户' }
  const page = await entryPage({ state: { accounts: [account, secondAccount] }, refresh: async () => {} })
  await page.initialize(); page.setType('TRANSFER')
  page.openModal('to')
  assert.equal(page.isSameAccountSelection(0), true)
  page.selectAccount(0)
  assert.equal(page.toIndex.value, 1); assert.equal(page.modal.value, 'to')
  page.modal.value = ''
  page.openModal('account')
  assert.equal(page.isSameAccountSelection(1), true)
  page.selectAccount(1)
  assert.equal(page.accountIndex.value, 0); assert.equal(page.modal.value, 'account')
})
test('日期与时间弹窗在确认前不写回，并使用中文日期展示', async () => {
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {} })
  await page.initialize(); const original = page.dateTime.value
  page.openModal('date'); page.draftDate.value = '2026-09-09'; page.draftTime.value = '22:03'
  assert.equal(page.dateTime.value, original); assert.equal(page.formatPickerDate(page.draftDate.value), '2026年09月09日')
  page.confirmModal()
  assert.equal(page.dateTime.value, '2026-09-09T22:03:00'); assert.equal(page.modal.value, '')
})
test('备注弹窗在完成前不写回，完成后更新备注', async () => {
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {} })
  await page.initialize(); page.openModal('note'); page.draftNote.value = '和朋友聚餐'
  assert.equal(page.note.value, '')
  page.confirmModal()
  assert.equal(page.note.value, '和朋友聚餐'); assert.equal(page.modal.value, '')
})
test('确定保存后提示成功并跳转首页，再记保存后保留表单上下文供下一笔录入', async () => {
  const payloads = []
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {}, createTransaction: async payload => { payloads.push(payload) } })
  await page.initialize()
  page.expression.value = '2+3×4'; page.note.value = '午餐'
  await page.save('again')
  assert.equal(payloads[0].amountCents, 1400)
  assert.equal(page.expression.value, ''); assert.equal(page.amount.value, ''); assert.equal(page.note.value, '')
  assert.equal(page.events.toasts.length, 0); assert.equal(page.events.tabSwitches.length, 0)
  const firstKey = payloads[0].idempotencyKey
  page.expression.value = '5'; await page.save('again')
  assert.notEqual(payloads[1].idempotencyKey, firstKey)
  page.expression.value = '8'; await page.save('home')
  assert.deepEqual(page.events.tabSwitches.at(-1), { url: '/pages/index/index' })
})
test('日历入口日期被用于新增账单且不改变账户默认逻辑', async () => {
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {} })
  page.hooks.load({ date: '2026-09-14' })
  assert.equal(page.dateTime.value.slice(0, 10), '2026-09-14')
})

test('日历快速切换日期时旧响应不能覆盖新选择', async () => {
  const requests = []
  const source = (await readFile(new URL('../src/pages/calendar/calendar.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { loadDay, selected, detail, dayLoading };', {
    '@dcloudio/uni-app': { onShow() {}, onPullDownRefresh() {} },
    '../../stores/ledger': { useLedger: () => ({ state: { token: 'test-session' } }) },
    '../../utils/api': { request: () => { const pending = deferred(); requests.push(pending); return pending.promise } },
    '../../utils/money': money,
  })
  page.selected.value = '2026-09-14'; const first = page.loadDay(page.selected.value)
  page.selected.value = '2026-09-15'; const second = page.loadDay(page.selected.value)
  assert.equal(page.detail.value, null); assert.equal(page.dayLoading.value, true)
  requests[1].resolve({ date: '2026-09-15', transactions: [] }); await second
  requests[0].resolve({ date: '2026-09-14', transactions: [] }); await first
  assert.equal(page.detail.value.date, '2026-09-15'); assert.equal(page.dayLoading.value, false)
})

test('账单写入成功后刷新失败不作为写入失败抛出', async () => {
  let writes = 0
  const ledgerModule = run(await readFile(new URL('../src/stores/ledger.ts', import.meta.url), 'utf8'), {
    '../utils/api': { request: async (url, options) => { if (options?.method === 'POST') { writes++; return {} } throw new Error('刷新失败') } },
    '../utils/money': money,
  })
  await ledgerModule.useLedger().createTransaction({ type: 'EXPENSE', amountCents: 100, accountId: '10', occurredAt: '2026-09-08T12:00' })
  assert.equal(writes, 1)
})

test('退款删除成功后立即移除记录，详情刷新失败也不回显旧记录', async () => {
  const hooks = {}
  const calls = []
  const ledger = { state: { accounts: [] }, refresh: async () => {} }
  const source = (await readFile(new URL('../src/pages/detail/detail.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { detail, id, loading, deleteOpen, saving, error, openDelete, confirmDelete };', {
    '@dcloudio/uni-app': { onLoad: fn => { hooks.load = fn }, onShow: fn => { hooks.show = fn } },
    '../../components/MoneyDisplay.vue': {}, '../../stores/ledger': { useLedger: () => ledger },
    '../../utils/api': { request: async (url, options) => {
      calls.push({ url, method: options?.method || 'GET' })
      if (options?.method === 'DELETE') return undefined
      throw new Error('详情刷新失败')
    } },
    '../../utils/money': money, '../../utils/entry': entryUtils,
    '../../utils/id': { stringId: value => value == null ? '' : String(value) },
  })
  page.id.value = '20'
  page.detail.value = {
    transaction: { id: '20', type: 'EXPENSE', amountCents: 750, originalAmountCents: 1000, hasRefund: true, accountId: '10', occurredAt: '2026-09-09T12:30:00', transactionNo: 'TRX-20', status: 'ACTIVE' },
    refundedCents: 250,
    effectiveCents: 750,
    refunds: [{ id: '31', refundNo: 'REF-31', amountCents: 250, createdAt: '2026-09-09T12:31:00' }],
  }
  page.loading.value = false
  page.openDelete({ type: 'refund', id: '31' })
  await page.confirmDelete()
  assert.deepEqual(calls.slice(0, 1), [{ url: '/api/app/transactions/refunds/31', method: 'DELETE' }])
  assert.equal(page.detail.value.refunds.length, 0)
  assert.equal(page.detail.value.refundedCents, 0)
  assert.equal(page.detail.value.effectiveCents, 1000)
  assert.equal(page.detail.value.transaction.amountCents, 1000)
  assert.equal(page.detail.value.transaction.hasRefund, false)
  assert.equal(page.deleteOpen.value, false)
  assert.equal(page.saving.value, false)
})

test('账单详情首次加载等待路由参数，避免 onShow 先发空 ID 请求', async () => {
  const hooks = {}
  const calls = []
  const detail = {
    transaction: { id: '20', type: 'EXPENSE', amountCents: 1000, originalAmountCents: 1000, hasRefund: false, accountId: '10', occurredAt: '2026-09-09T12:30:00', transactionNo: 'TRX-20' },
    refundedCents: 0,
    effectiveCents: 1000,
    refunds: [],
  }
  const source = (await readFile(new URL('../src/pages/detail/detail.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { id, detail, loading };', {
    '@dcloudio/uni-app': { onLoad: fn => { hooks.load = fn }, onShow: fn => { hooks.show = fn } },
    '../../components/MoneyDisplay.vue': {}, '../../stores/ledger': { useLedger: () => ({ state: { accounts: [] }, refresh: async () => {} }) },
    '../../utils/api': { request: async url => { calls.push(url); return detail } },
    '../../utils/money': money, '../../utils/entry': entryUtils,
    '../../utils/id': { stringId: value => value == null ? '' : String(value) },
  })
  hooks.show()
  assert.deepEqual(calls, [])
  hooks.load({ id: '20' })
  assert.deepEqual(calls, ['/api/app/transactions/20'])
  assert.equal(page.id.value, '20')
})
