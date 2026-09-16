import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'

const prototype = await readFile(new URL('../src/prototype.scss', import.meta.url), 'utf8')
const styles = await readFile(new URL('../src/styles.scss', import.meta.url), 'utf8')
const read = name => readFile(new URL(`../src/${name}`, import.meta.url), 'utf8')

test('共享样式允许认证页在小屏纵向滚动', () => {
  assert.match(prototype, /\.auth-page \{[^}]*overflow-x:hidden; overflow-y:auto;/)
  assert.doesNotMatch(prototype, /\.auth-page \{[^}]*overflow:hidden;/)
  assert.match(prototype, /\.auth-shell \{[^}]*env\(safe-area-inset-bottom\)/)
})

test('文本按钮和主要操作按钮明确居中并清除平台默认行高差异', () => {
  assert.match(prototype, /\.text-button \{[^}]*display:flex;[^}]*align-items:center;[^}]*justify-content:center;/)
  assert.match(prototype, /\.calculation-line \{[^}]*display:flex;[^}]*justify-content:center;/)
  assert.match(prototype, /\.filter-row button \{[^}]*display:flex;[^}]*align-items:center;[^}]*justify-content:center;/)
  assert.match(prototype, /\.back \{[^}]*display:flex;[^}]*align-items:center;/)
  assert.match(styles, /\.help-back \{[^}]*display:flex;[^}]*align-items:center;/)
})

test('记账页顶部导航避开微信胶囊且类型按钮显式锁定尺寸', async () => {
  const entry = await read('pages/entry/entry.vue')
  assert.match(entry, /<view class="page entry-page">\s*<view class="screen-nav">[\s\S]*<\/view>\s*<view class="entry-content">/)
  assert.match(prototype, /\.entry-page > \.screen-nav \{ flex-shrink:0; margin-left:0; margin-right:0; \}/)
  assert.match(prototype, /\/\* #ifdef MP-WEIXIN \*\/[\s\S]*\.entry-page \{ padding-top:calc\(var\(--status-bar-height, 0px\) \+ 44px \+ env\(safe-area-inset-top, 0px\)\); \}/)
  assert.match(prototype, /\.entry-type button \{[^}]*min-height:38px;[^}]*line-height:1;/)
})

test('弹层在两端都保留安全区和内容滚动边界', () => {
  assert.match(prototype, /\.modal-backdrop, \.asset-create-backdrop, \.month-picker-backdrop, \.entry-account-picker-backdrop, \.entry-date-picker-backdrop, \.entry-value-picker-backdrop, \.entry-note-picker-backdrop \{[^}]*env\(safe-area-inset-bottom\)/)
  assert.match(prototype, /\.center-modal \{[^}]*overflow:hidden;/)
  assert.match(prototype, /\.modal-content \{[^}]*overflow-y:auto;/)
  assert.match(prototype, /\.entry-date-picker-modal \{[^}]*height:calc\(100vh - 52px\); max-height:424px;/)
  assert.match(prototype, /\.entry-value-picker-modal \{[^}]*height:calc\(100vh - 52px\); max-height:460px;/)
  assert.match(prototype, /\.entry-note-picker-modal \{[^}]*height:calc\(100vh - 52px\); max-height:420px;/)
  assert.match(prototype, /\.entry-date-picker-modal, \.entry-value-picker-modal, \.entry-note-picker-modal \{[^}]*overflow-y:auto;/)
})

test('帮助页和固定资料操作栏使用动态视口与底部安全区', () => {
  assert.match(styles, /\.help-scroll \{[^}]*env\(safe-area-inset-bottom\)/)
  assert.match(styles, /\.help-page \{ height:100dvh; \}/)
  assert.match(styles, /\.profile-page \{ min-height:100dvh; \}/)
  assert.match(styles, /\.profile-actions \{[^}]*env\(safe-area-inset-bottom\)/)
})

test('微信小程序资料保存按钮显式覆盖原生按钮外观', () => {
  assert.match(styles, /\.profile-save-action \{[^}]*color:#fff;[^}]*background-color:#49ad9c;/)
  assert.match(styles, /\.profile-save-action::after \{[^}]*display:none;[^}]*border:0;/)
  assert.match(styles, /\.profile-password-confirm \{[^}]*color:#fff;[^}]*background-color:#49ad9c;/)
  assert.match(styles, /\.profile-password-action::after, \.profile-password-confirm::after \{[^}]*display:none;[^}]*border:0;/)
})

test('主题色使用微信小程序可直接编译的颜色值', async () => {
  const pageHeader = await read('components/PageHeader.vue')
  const themeVar = /var\(--(?:background|surface|primary|primary-dark|primary-light|text|muted|light|income|danger|liability|divider)\)/
  for (const source of [prototype, styles, pageHeader]) assert.doesNotMatch(source, themeVar)
  assert.match(prototype, /page, :root \{[\s\S]*--background:#f7f8f7;/)
  assert.match(prototype, /color:#171a1a; background:#f7f8f7;/)
  assert.match(styles, /\.help-page \{[^}]*background:#f7f8f7;/)
  assert.match(pageHeader, /\.page-subtitle \{[^}]*color:#858b8b;/)
})

test('核心可滚动页面仍保留独立 scroll-view 结构', async () => {
  const pages = await Promise.all([
    read('pages/index/index.vue'),
    read('pages/calendar/calendar.vue'),
    read('pages/account/account.vue'),
    read('pages/entry/entry.vue'),
    read('pages/help/help.vue'),
  ])
  for (const source of pages) assert.match(source, /<scroll-view[\s\S]*scroll-y/)
})
