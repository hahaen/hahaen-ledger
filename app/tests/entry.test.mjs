import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'

const compile = async path => ts.transpileModule(await readFile(new URL(path, import.meta.url), 'utf8'), { compilerOptions: { target: ts.ScriptTarget.ESNext, module: ts.ModuleKind.ESNext } }).outputText
const moduleUrl = source => `data:text/javascript;base64,${Buffer.from(source).toString('base64')}`
const moneyUrl = moduleUrl(await compile('../src/utils/money.ts'))
const money = await import(moneyUrl)
const entry = await import(moduleUrl((await compile('../src/utils/entry.ts')).replace("'./money'", JSON.stringify(moneyUrl))))

test('千元以上编辑与退款金额可以无损往返，展示仍保留分组', () => {
  for (const value of [1, 29, 100, 100001, 850000, 99999999999]) assert.equal(money.cents(money.inputYuan(value)), value)
  assert.equal(money.formatYuan(850000), '8,500')
  assert.equal(money.formatYuan(-428000), '-4,280')
  for (const value of ['0', '-1', '1,000', '1.001', '1000000000', 'NaN']) assert.throws(() => money.cents(value))
})
test('计算器按优先级计算，最终舍入且不引入浮点误差', () => {
  for (const [input, result] of [['0.1+0.2', '0.30'], ['1000+0.01', '1000.01'], ['2+3×4', '14'], ['1÷3×3', '1'], ['0.01÷2×2', '0.01'], ['1÷8', '0.13'], ['5−2', '3'], ['0+1', '1'], ['999999999.99×1', '999999999.99']]) assert.equal(entry.calculateAmount(input), result)
  for (const input of ['1÷0', '1+', '1−2', '0.01÷100', '999999999.99+1']) assert.throws(() => entry.calculateAmount(input))
})
test('日期校验拒绝自动滚动日期和数据库范围外年份', () => {
  for (const value of ['2024-02-29T23:59', '2026-09-08T00:00:00']) assert.equal(entry.validLocalDateTime(value), true)
  for (const value of ['2026-02-29T12:00', '2026-04-31T12:00', '2026-09-08T24:00', '2026-09-08T12:60', '0999-01-01T00:00', '2026-13-01T12:00']) assert.equal(entry.validLocalDateTime(value), false)
})
