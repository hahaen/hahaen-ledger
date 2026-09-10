import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'

const accountPage = await readFile(new URL('../src/pages/account/account.vue', import.meta.url), 'utf8')
const prototypeStyles = await readFile(new URL('../src/prototype.scss', import.meta.url), 'utf8')

test('账户详情固定筛选按钮并让日期和流水区域独立滚动', () => {
  const filterIndex = accountPage.indexOf('<view class="filter-row">')
  const listIndex = accountPage.indexOf('<scroll-view scroll-y :show-scrollbar="false" class="account-record-list">')
  assert.ok(filterIndex >= 0 && listIndex > filterIndex)
  assert.match(accountPage.slice(listIndex), /class="date-heading"/)
  assert.match(accountPage.slice(listIndex), /class="transaction-list"/)
  assert.match(prototypeStyles, /\.account-page\s*\{[^}]*height:100vh[^}]*overflow:hidden/)
  assert.match(prototypeStyles, /\.account-page \.detail-section\s*\{[^}]*flex:1[^}]*min-height:0/)
  assert.match(prototypeStyles, /\.account-record-list\s*\{[^}]*flex:1[^}]*height:0[^}]*min-height:0[^}]*overflow-y:auto/)
  assert.match(prototypeStyles, /\.account-record-list \.date-heading\s*\{[^}]*position:sticky/)
  assert.match(prototypeStyles, /\.account-record-list \.date-heading\s*\{[^}]*top:0/)
  assert.match(prototypeStyles, /\.account-record-list \.date-heading\s*\{[^}]*z-index:2/)
})
