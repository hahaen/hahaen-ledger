import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import ts from 'typescript'
const source = fs.readFileSync(new URL('../src/utils/itemCostChart.ts', import.meta.url), 'utf8')
const compiled = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 } }).outputText
const exports = {}
new Function('exports', compiled)(exports)
const { itemCostScale } = exports

test('十年日均成本保留全周期，后半段不再被首日高值压平', () => {
  const values = Array.from({ length: 31 }, (_, i) => 1000000 / (1 + Math.floor(i * 3649 / 30)))
  const scale = itemCostScale(values)
  assert.equal(scale.logarithmic, true)
  assert.equal(scale.position(values[0]), 1)
  assert.ok(scale.position(values.at(-1)) > 0.1)
  assert.ok((scale.position(values[15]) - scale.position(values.at(-1))) * 144 > 5)
  values.forEach((value, i) => {
    assert.ok(Number.isFinite(scale.position(value)))
    if (i) assert.ok(scale.position(value) < scale.position(values[i - 1]))
  })
  scale.ticks.forEach((value, i) => assert.ok(Math.abs(scale.position(value) - i / 4) < 1e-10))
})
test('短期和分币小金额保留线性刻度', () => {
  for (const values of [[1000, 500, 250], [10, 1, 0.1]]) {
    const scale = itemCostScale(values)
    assert.equal(scale.logarithmic, false)
    assert.equal(scale.position(values[0]), 1)
    assert.equal(scale.position(values[1]), values[1] / values[0])
  }
})
test('零、空数据与单点保持有限坐标', () => {
  for (const values of [[], [0], [0, 0], [1000]]) {
    const scale = itemCostScale(values)
    assert.equal(scale.logarithmic, false)
    assert.ok(scale.ticks.every(Number.isFinite))
    values.forEach(value => assert.ok(Number.isFinite(scale.position(value))))
  }
})
test('退役出售超过购入价时，负成本与零基线正确保留', () => {
  const scale = itemCostScale([300000, 1000, -55])
  assert.equal(scale.logarithmic, true)
  assert.equal(scale.position(-55), 0)
  assert.equal(scale.position(300000), 1)
  assert.ok(scale.position(0) > 0)
  assert.ok(scale.position(1000) > scale.position(0))
  assert.ok(scale.ticks[0] < 0)
  const linear = itemCostScale([-100, -200])
  assert.equal(linear.position(-200), 0)
  assert.equal(linear.position(0), 1)
})
