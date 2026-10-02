import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'

const calendarPage = await readFile(new URL('../src/pages/calendar/calendar.vue', import.meta.url), 'utf8')
const prototypeStyles = await readFile(new URL('../src/prototype.scss', import.meta.url), 'utf8')

test('日历标题和底栏位于统一主体滚动区域外，账单自然展开', () => {
  assert.match(calendarPage, /<PageHeader[^>]*\/>\s*<scroll-view[^>]*class="calendar-content"/)
  assert.match(calendarPage, /<view class="calendar-content-inner">[\s\S]*class="calendar-card"[\s\S]*class="day-summary"[\s\S]*class="date-heading calendar-date"[\s\S]*<view class="calendar-transaction-list transaction-list">/)
  assert.match(calendarPage, /<\/scroll-view>\s*<BottomNav active="calendar"/)
  assert.equal((calendarPage.match(/<scroll-view/g) || []).length, 1)
  assert.match(prototypeStyles, /\.calendar-content\s*\{[^}]*flex:1 1 0[^}]*height:0[^}]*min-height:0/)
  assert.doesNotMatch(prototypeStyles, /\.calendar-transaction-list\s*\{[^}]*(height:|overflow-y:|flex:)/)
  assert.doesNotMatch(prototypeStyles, /max-height:740px/)
})

test('日历日期格使用跨端 view，避免微信原生 button/text 覆盖选中态', () => {
  assert.match(calendarPage, /<view v-for="day in visibleDays"[^>]*class="\['calendar-cell'/)
  assert.match(calendarPage, /<view class="day-num">\{\{ day\.day \}\}<\/view>/)
  assert.doesNotMatch(calendarPage, /<button v-for="day in visibleDays"/)
  assert.match(prototypeStyles, /\.calendar-cell\s*\{[^}]*display:flex[^}]*flex-direction:column/)
  assert.match(prototypeStyles, /\.calendar-cell\.selected \.day-num\s*\{[^}]*background:#49ad9c !important[^}]*background-color:#49ad9c !important[^}]*color:#fff !important/)
})
