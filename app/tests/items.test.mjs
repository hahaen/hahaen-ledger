import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import ts from 'typescript'
const source = fs.readFileSync(new URL('../src/utils/items.ts', import.meta.url), 'utf8')
const compiled = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 } }).outputText
const exports = {}
new Function('exports', compiled)(exports)
test('物品金额精确到分，允许赠品和未出售，拒绝负数和超限', () => {
  assert.equal(exports.itemAmount('0'), 0)
  assert.equal(exports.itemAmount('0.29'), 29)
  assert.equal(exports.itemAmount('999999999.99'), 99999999999)
  for (const input of ['', '-1', '1.001', '1e3', '1000000000', 'NaN']) assert.throws(() => exports.itemAmount(input))
})
test('日期校验包含闰日和自然日期，拒绝未来与无效日期', () => {
  exports.validItemDate('2000-01-01', '2026-09-28')
  exports.validItemDate('2024-02-29', '2026-09-28')
  for (const value of ['1999-12-31', '2025-02-29', '2026-09-29', '2026-13-01']) assert.throws(() => exports.validItemDate(value, '2026-09-28'))
})
test('失败重试复用幂等键，修改数据后更换', () => {
  const submission = exports.itemSubmission()
  const first = submission({ name: '耳机', priceCents: 100 })
  assert.match(first.idempotencyKey, /^[A-Za-z0-9_-]{16,64}$/)
  assert.equal(first.idempotencyKey, submission({ name: '耳机', priceCents: 100 }).idempotencyKey)
  assert.notEqual(first.idempotencyKey, submission({ name: '耳机', priceCents: 101 }).idempotencyKey)
})
test('物品导航位于资产右边，五个一级导航保持跨端一致', () => {
  for (const path of ['../src/pages.json', '../pages.json']) {
    const config = JSON.parse(fs.readFileSync(new URL(path, import.meta.url)))
    assert.deepEqual(config.tabBar.list.map(item => item.text), ['首页', '日历', '资产', '物品', '我的'])
  }
})
test('物品日期弹窗只渲染 2000 年起的普通年份列表', () => {
  const picker = fs.readFileSync(new URL('../src/components/EntryDateTimePicker.vue', import.meta.url), 'utf8')
  assert.match(picker, /v-for="value in years"/)
  assert.match(picker, /length: 100[\s\S]*2000 \+ index/)
  assert.doesNotMatch(picker, /visibleYears|yearTopSpacer|yearBottomSpacer/)
  const yearColumn = picker.match(/<text>年份<\/text>[\s\S]*?<\/scroll-view>/)?.[0] || ''
  assert.equal((yearColumn.match(/entry-value-picker-year-spacer/g) || []).length, 2)
  assert.match(picker, /centeredYearScrollTop/)
  assert.match(picker, /centeredValue\(years, event, centerOffset\)/)
  for (const path of ['../src/components/ItemEditor.vue', '../src/components/ItemProfileEditor.vue']) {
    const source = fs.readFileSync(new URL(path, import.meta.url), 'utf8')
    assert.match(source, /ITEM_MIN_DATE/)
  }
  assert.doesNotMatch(picker, /entry-value-picker-current-year/)
})
