// 先执行 build:mp-weixin，再运行本探针；模拟渲染数据，不替代微信真机验收。
const fs = require('node:fs')
const path = require('node:path')
const vm = require('node:vm')
const assert = require('node:assert/strict')
const root = path.resolve(__dirname, '../dist/build/mp-weixin')
const vendor = {
  defineComponent: value => value, computed: getter => ({ get value() { return getter() } }),
  ref: value => ({ value }), getCurrentInstance: () => null,
  onMounted: () => {}, onBeforeUnmount: () => {}, _export_sfc: value => value,
  e: (...objects) => Object.assign({}, ...objects), t: String, n: value => value,
  s: value => value, f: (values, callback) => values.map(callback),
}

for (const mode of ['ignored', 'unavailable']) {
  const cache = new Map()
  function load(relative) {
    const filename = path.resolve(root, relative)
    if (cache.has(filename)) return cache.get(filename)
    let component
    const context = vm.createContext({ exports: {}, wx: { createComponent: value => { component = value } },
      require: name => name.endsWith('/vendor.js') ? vendor : load(path.relative(root, path.resolve(path.dirname(filename), name))),
    })
    vm.runInContext(mode === 'ignored'
      ? 'Number.prototype.toLocaleString = function() { return String(this.valueOf()) }'
      : 'Intl = undefined; Number.prototype.toLocaleString = function() { throw new Error("locale unavailable") }', context)
    vm.runInContext(fs.readFileSync(filename, 'utf8'), context)
    const result = component || context.exports
    cache.set(filename, result)
    return result
  }
  const money = load('utils/money.js')
  assert.equal(money.formatYuan(49779.4934899), '497.79')
  assert.equal(money.formatYuan(2769500), '27,695')
  const component = load('components/MoneyDisplay.js')
  const props = { value: 23571.42857143, prefix: '' }
  assert.equal(component.setup(props)(props).c, '235.71')
  const chart = load('components/ItemCostChart.js')
  const chartProps = { points: [
    { date: '2026-09-22', day: 1, dailyCostCents: 495000 },
    { date: '2026-10-12', day: 21, dailyCostCents: 23571.42857143 },
  ] }
  const rendered = chart.setup(chartProps)(chartProps)
  assert.deepEqual(Array.from(rendered.a, tick => tick.a), ['0', '7.39', '69.36', '589.23', '4,950'])
  console.log(`PASS: 微信生产包 ${mode} 金额组件及图表渲染数据`)
}
