import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import assert from 'node:assert/strict'
import ts from 'typescript'
const read = path => readFileSync(new URL(path, import.meta.url), 'utf8')
const entry = read('../src/pages/entry/entry.vue')
function runtime(context) {
  const name = entry.includes('function pressCalculatorKey(') ? 'pressCalculatorKey' : 'handleCalculatorTouchStart'
  const start = entry.indexOf(`function ${name}(`)
  const open = entry.indexOf(') {', start) + 2
  let depth = 0, end = open
  for (; end < entry.length; end++) {
    if (entry[end] === '{') depth++
    if (entry[end] === '}' && --depth === 0) break
  }
  const code = ts.transpileModule(`const { locked, triggerCalculatorFeedback, appendKey, save } = context; ${entry.slice(start, end + 1)}; exports.press = ${name}`, { compilerOptions: { module: ts.ModuleKind.CommonJS } }).outputText
  const exports = {}
  new Function('context', 'exports', code)(context, exports)
  return exports.press
}
test('小程序普通事件对象不依赖 DOM，数字和操作符每次只反馈一次', () => {
  const events = []
  const press = runtime({ locked: {value:false}, triggerCalculatorFeedback:()=>events.push('feedback'), appendKey:key=>events.push(key), save:mode=>events.push(mode) })
  for (const key of ['1','0','.','⌫','+','−','×','÷']) press(key)
  assert.deepEqual(events, ['1','0','.','⌫','+','−','×','÷'].flatMap(key=>['feedback',key]))
})
test('保存按钮只反馈一次，禁用时不反馈也不保存', () => {
  const events = [], locked = {value:false}
  const press = runtime({locked,triggerCalculatorFeedback:()=>events.push('feedback'),appendKey:key=>events.push(key),save:mode=>events.push(mode)})
  press('confirm'); press('C'); locked.value=true; press('1'); press('confirm')
  assert.deepEqual(events,['feedback','home','feedback','again'])
})
test('内部触摸滚动不冒泡到页面或遮罩禁止滚动处理', () => {
  // 主体由跨端 scroll-view 承担滚动，不绑定拦截手势。
  assert.match(entry, /<scroll-view scroll-y :show-scrollbar="false" class="entry-content">/)
  assert.match(entry, /class="entry-date-picker-backdrop">/)
  for (const [path, cls, count] of [['EntryDateTimePicker.vue','entry-value-picker-scroll',5],['MonthPicker.vue','date-picker-scroll',2]]) {
    const source = read(`../src/components/${path}`)
    const nodes = [...source.matchAll(new RegExp(`<scroll-view[^>]*class="${cls}"[^>]*>`, 'g'))]
    assert.equal(nodes.length,count)
    for (const [node] of nodes) assert.doesNotMatch(node, /@touchmove/)
    assert.match(source, /class="picker-touch-mask"[^>]*@touchmove\.stop\.prevent \/>/)
    assert.match(source, /-backdrop">/)
    assert.match(source, /#ifdef H5\s+event\.stopPropagation\(\)\s+\/\/ #endif/)
  }
})

function feedbackRuntime(platform, context) {
  const source = read('../src/utils/calculatorFeedback.ts')
  const processed = source.replace(/\/\/ #ifdef (H5|MP-WEIXIN)\n([\s\S]*?)\/\/ #endif/g, (_, name, block) => name === platform ? block : '')
  const exports = {}
  const code = ts.transpileModule(processed, {compilerOptions:{module:ts.ModuleKind.CommonJS}}).outputText
  new Function('exports','uni','navigator',code)(exports,context.uni,context.navigator)
  return exports.triggerCalculatorFeedback
}
test('微信请求轻振动，旧设备失败时退回默认短振动', () => {
  const calls = []
  const trigger = feedbackRuntime('MP-WEIXIN',{uni:{vibrateShort:options=>calls.push(options)}})
  trigger(); assert.equal(calls.length,1); assert.equal(calls[0].type,'light')
  calls[0].fail(); assert.equal(calls.length,2); assert.equal(calls[1].type,undefined)
})
test('H5 请求 15ms 振动，不支持的浏览器不影响按键执行', () => {
  const calls = []
  feedbackRuntime('H5',{navigator:{vibrate:value=>calls.push(value)}})()
  assert.deepEqual(calls,[15])
  assert.doesNotThrow(feedbackRuntime('H5',{navigator:{}}))
  assert.doesNotThrow(feedbackRuntime('H5',{}))
})
