import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import ts from 'typescript'

const source = fs.readFileSync(new URL('../src/components/EntryDateTimePicker.vue', import.meta.url), 'utf8')
const styles = fs.readFileSync(new URL('../src/prototype.scss', import.meta.url), 'utf8')
const optionHeight = 48
const viewportHeight = 240

function functionSource(name) {
  const start = source.indexOf(`function ${name}(`)
  assert.notEqual(start, -1, `${name} must be used by the picker`)
  const open = source.indexOf(') {', start) + 2
  let depth = 0
  for (let index = open; index < source.length; index++) {
    if (source[index] === '{') depth++
    if (source[index] === '}' && --depth === 0) return source.slice(start, index + 1)
  }
  throw new Error(`Cannot read ${name}`)
}

const runtime = {}
const script = ts.transpileModule(`
  const optionHeight = 48, centerOffset = optionHeight * 2;
  ${functionSource('centeredScrollTop')}
  ${functionSource('centeredValue')}
  exports.centeredScrollTop = centeredScrollTop;
  exports.centeredValue = centeredValue;
`, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.CommonJS } }).outputText
new Function('exports', script)(runtime)

function columnLayout(label) {
  const start = source.indexOf(`<view class="entry-value-picker-column"><text>${label}</text>`)
  assert.notEqual(start, -1)
  const markup = source.slice(start, source.indexOf('</scroll-view>', start))
  const spacerClasses = [...markup.matchAll(/class="(entry-value-picker-(?:year-)?spacer)"/g)].map(match => match[1])
  const heights = spacerClasses.map(name => {
    const match = styles.match(new RegExp(`\\.${name} \\{ height:(\\d+)px;`))
    assert.ok(match, `${name} needs a declared height`)
    return Number(match[1])
  })
  return { before: heights[0] || 0, after: heights[1] || 0 }
}

function selectedAfterScroll(label, values, chosen) {
  const { before, after } = columnLayout(label)
  const target = runtime.centeredScrollTop(values.indexOf(chosen))
  const max = Math.max(0, before + values.length * optionHeight + after - viewportHeight)
  const actual = Math.min(target, max)
  return runtime.centeredValue(values, { detail: { scrollTop: actual } }, before)
}

test('滚动容器触底后仍高亮用户选择的 9 月，而非跳回 7 月', () => {
  assert.equal(selectedAfterScroll('月份', Array.from({ length: 9 }, (_, index) => index + 1), 9), 9)
})

test('滚动容器触顶后仍高亮用户选择的 1 日，而非跳到 3 日', () => {
  assert.equal(selectedAfterScroll('日期', Array.from({ length: 31 }, (_, index) => index + 1), 1), 1)
})

test('年份两端及时间滚轮两端都能停在所选项', () => {
  const years = Array.from({ length: 100 }, (_, index) => 2000 + index)
  assert.equal(selectedAfterScroll('年份', years, 2000), 2000)
  assert.equal(selectedAfterScroll('年份', years, 2099), 2099)
  for (const [label, length] of [['小时', 24], ['分钟', 60]]) {
    const values = Array.from({ length }, (_, index) => index)
    assert.equal(selectedAfterScroll(label, values, 0), 0)
    assert.equal(selectedAfterScroll(label, values, length - 1), length - 1)
  }
})

test('滚轮能浏览 2099，但物品传入当天上限时不能确认未来日期', () => {
  const result = {}
  const events = []
  const context = {
    props: { mode: 'date' }, emit: (...event) => events.push(event),
    year: { value: 2099 }, month: { value: 12 }, day: { value: 31 },
    hour: { value: 0 }, minute: { value: 0 }, validationError: { value: '' },
    minDate: '2000-01-01', maxDate: '2026-09-29',
  }
  const code = ts.transpileModule(`
    const { props, emit, year, month, day, hour, minute, validationError, minDate, maxDate } = context;
    ${functionSource('confirm')}
    exports.confirm = confirm;
  `, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.CommonJS } }).outputText
  new Function('context', 'exports', code)(context, result)
  result.confirm()
  assert.deepEqual(events, [])
  assert.match(context.validationError.value, /2026-09-29/)
  context.year.value = 2026
  context.month.value = 9
  context.day.value = 29
  result.confirm()
  assert.deepEqual(events, [['select', '2026-09-29']])
})
