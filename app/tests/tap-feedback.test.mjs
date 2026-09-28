import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'

const prototype = await readFile(new URL('../src/prototype.scss', import.meta.url), 'utf8')
const feedback = await readFile(new URL('../src/utils/calculatorFeedback.ts', import.meta.url), 'utf8')
const app = await readFile(new URL('../src/App.vue', import.meta.url), 'utf8')
const entry = await readFile(new URL('../src/pages/entry/entry.vue', import.meta.url), 'utf8')

test('共享样式保留按钮和可点击控件的按压动画', () => {
  assert.match(prototype, /button, \[role="button"\], switch, picker, a\[href\], \.account-reorder-action/)
  assert.match(prototype, /button:active:not\(\[disabled\]\), \[role="button"\]:active:not\(\[aria-disabled="true"\]\)/)
  assert.match(prototype, /transform:scale\(\.985\)/)
  assert.match(prototype, /prefers-reduced-motion:reduce/)
})

test('全局点击不触发振动，金额键盘保留平台轻振动', () => {
  assert.doesNotMatch(app, /installH5TapFeedback|tapFeedback/)
  assert.doesNotMatch(feedback, /document\.addEventListener|pointerdown|handleMpTouchStart/)
  assert.match(entry, /class="keypad"[^>]*@touchstart\.capture="handleCalculatorTouchStart"/)
  assert.match(entry, /target\.closest\('\.key'\)/)
  assert.match(entry, /key\.matches\(':disabled, \[disabled\]'\)/)
  assert.match(feedback, /navigator\.vibrate\(8\)/)
  assert.match(feedback, /#ifdef MP-WEIXIN[\s\S]*uni\.vibrateShort\(\{ type: 'light'/)
  assert.match(feedback, /#ifdef H5[\s\S]*navigator\.vibrate\(8\)/)
})
