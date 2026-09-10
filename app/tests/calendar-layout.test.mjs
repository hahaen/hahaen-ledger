import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'

const calendarPage = await readFile(new URL('../src/pages/calendar/calendar.vue', import.meta.url), 'utf8')
const prototypeStyles = await readFile(new URL('../src/prototype.scss', import.meta.url), 'utf8')

test('日历页仅让记账记录区域独立滚动', () => {
  assert.match(calendarPage, /<scroll-view\s+scroll-y[^>]*class="calendar-transaction-list transaction-list"/)
  assert.match(prototypeStyles, /\.calendar-page\s*\{[^}]*height:100vh[^}]*overflow:hidden/)
  assert.match(prototypeStyles, /\.calendar-transaction-list\s*\{[^}]*flex:1[^}]*height:0[^}]*min-height:0[^}]*overflow-y:auto/)
})
