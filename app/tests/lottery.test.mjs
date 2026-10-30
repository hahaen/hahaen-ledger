import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'
import { createRequire } from 'node:module'
const require = createRequire(import.meta.url)
const transpile = source => ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ESNext } }).outputText
const helpers = {}
new Function('exports', transpile(await readFile(new URL('../src/utils/lottery.ts', import.meta.url), 'utf8')))(helpers)
const base = () => ({ game: 'DLT', name: '', red: '35 5 1 20 3', blue: '12 1', multiple: 1, additional: false, issue: '26124', periods: 1, purchasedOn: helpers.today(), note: '', confirmedPurchase: true, idempotencyKey: '' })
test('号码规范化、重复、边界与未知奖金状态', () => {
  assert.equal(helpers.validateLottery(base(), 'single').red, '01,03,05,20,35')
  assert.throws(() => helpers.validateLottery({ ...base(), red: '1 1 2 3 4' }, 'single'))
  assert.throws(() => helpers.validateLottery({ ...base(), multiple: 1.5 }, 'single'))
  assert.throws(() => helpers.validateLottery({ ...base(), periods: 21 }, 'purchase'))
  assert.throws(() => helpers.validateLottery({ ...base(), confirmedPurchase: false }, 'purchase'))
  assert.throws(() => helpers.validateLottery({ ...base(), name: '' }, 'plan'))
  assert.equal(helpers.resultLabel({ result: 'REVIEW', prizeCents: null }), '待核实')
  assert.equal(helpers.deliveryLabel('ACCEPTED'), '渠道已受理')
  assert.equal(helpers.resultLabel({ result: 'WAITING', issue: null }), '期号待核对')
})
test('计划提醒展示区分预估、过期同步、已受理和不发送状态', () => {
  const progress = { estimatedReminderDate: '2026-11-21', reminderState: 'WAITING' }
  assert.equal(helpers.reminderLabel(progress), '2026-11-21 晚开奖后')
  assert.match(helpers.reminderLabel({ ...progress, reminderState: 'WAITING_SYNC' }), /等待开奖数据同步/)
  assert.match(helpers.reminderLabel({ ...progress, reminderState: 'PAUSED' }), /已暂停提醒/)
  assert.match(helpers.reminderLabel({ ...progress, reminderState: 'ACCEPTED' }), /已受理，待续买/)
  assert.match(helpers.reminderLabel({ reminderState: 'NO_PURCHASE' }), /登记实际购买后计算/)
  assert.match(helpers.reminderLabel({ reminderState: 'UNCONFIRMED' }), /待官方期次核对/)
  assert.match(helpers.reminderLabel({ reminderState: 'NO_CHANNEL' }), /配置通知渠道/)
  assert.match(helpers.reminderLabel(), /刷新重试/)
})
async function editor(api) {
  const source = await readFile(new URL('../src/pages/lottery-editor/lottery-editor.vue', import.meta.url), 'utf8')
  const script = source.match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const exposed = {}, hooks = {}, navigations = []
  const dependencies = { vue: require('vue'), '@dcloudio/uni-app': { onLoad: cb => { hooks.load = cb } }, '../../utils/api': { lotteryApi: api, request: async () => ({ lotteryEnabled: true }) }, '../../utils/lottery': helpers }
  new Function('require', 'exports', 'uni', 'getCurrentPages', transpile(script + '\nexport { initialize, form, save, error, loading, cost, leave, discard }'))(name => dependencies[name] || {}, exposed, { showToast() {}, redirectTo: o => navigations.push(o.url), navigateBack: () => navigations.push('back') }, () => ['page'])
  await exposed.initialize({ mode: 'single', game: 'DLT' })
  return { ...exposed, navigations }
}
test('保存失败保留输入与幂等键，改动请求使用新键，成功进入详情', async () => {
  const calls = []; let fail = true
  const page = await editor({ create: async (kind, body) => { calls.push({ ...body }); if (fail) throw new Error('暂时失败'); return '1234567890123456789' } })
  Object.assign(page.form, { red: '1 2 3 4 5', blue: '1 2', issue: '26124', confirmedPurchase: true })
  await page.save(); await page.save(); assert.equal(calls.length, 2); assert.equal(calls[0].idempotencyKey, calls[1].idempotencyKey); assert.equal(page.form.red, '1 2 3 4 5')
  page.form.multiple = '2'; fail = false; await page.save(); assert.notEqual(calls[2].idempotencyKey, calls[1].idempotencyKey); assert.equal(page.cost.value, 400); assert.match(page.navigations[0], /kind=entry&id=1234567890123456789/)
})
test('保存前确认实际购买，离开有未保存内容确认', async () => {
  const calls = []; const page = await editor({ create: async () => calls.push('create') })
  page.form.red = '1 2 3 4 5'; page.form.blue = '1 2'; page.form.issue = '26124'; await page.save(); assert.equal(calls.length, 0)
  page.leave(); assert.equal(page.discard.value, true)
})
