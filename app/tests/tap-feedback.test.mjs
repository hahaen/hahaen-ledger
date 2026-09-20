import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'

const prototype = await readFile(new URL('../src/prototype.scss', import.meta.url), 'utf8')
const feedback = await readFile(new URL('../src/utils/tapFeedback.ts', import.meta.url), 'utf8')
const app = await readFile(new URL('../src/App.vue', import.meta.url), 'utf8')
const home = await readFile(new URL('../src/pages/index/index.vue', import.meta.url), 'utf8')
const bottomNav = await readFile(new URL('../src/components/BottomNav.vue', import.meta.url), 'utf8')

test('共享按压反馈覆盖按钮和明确标记的可点击控件', () => {
  assert.match(prototype, /button, \[role="button"\], switch, picker, a\[href\], \.account-reorder-action/)
  assert.match(prototype, /button:active:not\(\[disabled\]\), \[role="button"\]:active:not\(\[aria-disabled="true"\]\)/)
  assert.match(prototype, /transform:scale\(\.985\)/)
  assert.match(prototype, /prefers-reduced-motion:reduce/)
})

test('H5 使用捕获式轻触振动，禁用控件和重复事件不触发', () => {
  assert.match(feedback, /navigator\.vibrate\(8\)/)
  assert.match(feedback, /document\.addEventListener\('pointerdown', handlePointerDown, true\)/)
  assert.match(feedback, /:disabled, \[disabled\], \[aria-disabled="true"\]/)
  assert.match(feedback, /now - lastFeedbackAt < 40/)
  assert.match(app, /installH5TapFeedback\(\)/)
  assert.match(feedback, /handleMpTouchStart/)
  assert.match(home, /@touchstart\.capture="handleMpTouchStart"/)
  assert.match(bottomNav, /@touchstart\.capture="handleMpTouchStart"/)
  assert.match(bottomNav, /@touchstart="handleMpDirectTouchStart"/)
  assert.doesNotMatch(bottomNav, /@touchstart\.stop="handleMpDirectTouchStart"/)
  assert.match(bottomNav, /data-tap-feedback="true"/)
})

test('微信小程序走轻振动 API，保留平台条件编译', () => {
  assert.match(feedback, /#ifdef MP-WEIXIN[\s\S]*uni\.vibrateShort\(\{ type: 'light'/)
  assert.match(feedback, /handleMpDirectTouchStart/)
  assert.match(feedback, /#ifdef H5[\s\S]*navigator\.vibrate\(8\)/)
})
