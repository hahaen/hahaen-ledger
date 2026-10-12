import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import vm from 'node:vm'
import ts from 'typescript'

const moneySource = await readFile(new URL('../src/utils/money.ts', import.meta.url), 'utf8')
const chartSource = await readFile(new URL('../src/utils/itemCostChart.ts', import.meta.url), 'utf8')
const compile = source => ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS } }).outputText

function runtime(mode) {
  const context = vm.createContext({ exports: {} })
  if (mode === 'ignored') vm.runInContext('Number.prototype.toLocaleString = function() { return String(this.valueOf()) }', context)
  if (mode === 'unavailable') vm.runInContext('Intl = undefined; Number.prototype.toLocaleString = function() { throw new Error("locale unavailable") }', context)
  vm.runInContext(compile(moneySource), context)
  return context.exports
}

const cases = [
  [23571.42857143, '235.71'], [49779.4934899, '497.79'],
  [2769500, '27,695'], [495000, '4,950'], [30.19390582, '0.30'],
  [0, '0'], [-0, '0'], [1, '0.01'], [120, '1.20'],
  [100.5, '1.01'], [-100.5, '-1.01'], [0.5, '0.01'], [-0.5, '-0.01'],
  [99.5, '1'], [99999.5, '1,000'], [-0.49, '0'],
  [-428000, '-4,280'], [99999999999, '999,999,999.99'],
  ['23571.42857143', '235.71'], [null, '0'], [undefined, '0'],
  [NaN, '0'], [Infinity, '0'], ['invalid', '0'],
]

for (const mode of ['normal', 'ignored', 'unavailable']) {
  test(`金额展示在 ${mode} 运行时保留舍入、分组与符号`, () => {
    const money = runtime(mode)
    for (const [value, expected] of cases) assert.equal(money.formatYuan(value), expected, String(value))
    assert.equal(money.inputYuan(-428001), '-4280.01')
    assert.equal(money.cents('4280.01'), 428001)
  })
}

async function component(path, money, props, extra = {}, exposed) {
  const source = await readFile(new URL(path, import.meta.url), 'utf8')
  const script = source.match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const vue = {
    computed: getter => ({ get value() { return getter() } }), ref: value => ({ value }),
    getCurrentInstance: () => null, nextTick: async () => {}, onMounted: () => {}, onBeforeUnmount: () => {},
  }
  const context = vm.createContext({ exports: {}, defineProps: () => props,
    withDefaults: (value, defaults) => ({ ...defaults, ...value }),
    require: name => name === 'vue' ? vue : name === '../utils/money' ? money : extra[name],
  })
  vm.runInContext(compile(script + '\nexports.result = ' + exposed), context)
  return context.exports.result
}

test('共享 MoneyDisplay 与实际图表刻度在 Android 兼容运行时输出有限小数', async () => {
  const money = runtime('ignored')
  const display = await component('../src/components/MoneyDisplay.vue', money, { value: 23571.42857143 }, {}, 'displayValue')
  assert.equal(display.value, '235.71')
  const context = vm.createContext({ exports: {} })
  vm.runInContext(compile(chartSource), context)
  const chart = context.exports
  const axisLabel = await component('../src/components/ItemCostChart.vue', money, { points: [] },
    { '../utils/itemCostChart': chart }, 'axisLabel')
  const labels = chart.itemCostScale([495000, 23571.42857143]).ticks.map(axisLabel)
  assert.deepEqual(Array.from(labels), ['0', '7.39', '69.36', '589.23', '4,950'])
  assert.equal(axisLabel(1000000), '1.0万')
  assert.equal(axisLabel(-10000000000), '-1.0亿')
})
