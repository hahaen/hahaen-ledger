import { readFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'

const require = createRequire(import.meta.url)
const transpile = source => ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ESNext, module: ts.ModuleKind.CommonJS } }).outputText
const defaultUni = { getStorageSync: () => '', setStorageSync() {}, showToast() {}, navigateBack() {}, switchTab() {} }
const run = (source, dependencies = {}, uni = defaultUni) => {
  const exports = {}
  new Function('require', 'exports', 'uni', 'getCurrentPages', 'setTimeout', transpile(source))(
    name => dependencies[name] || require(name), exports,
    uni, () => [], callback => { callback(); return 0 })
  return exports
}
const money = run(await readFile(new URL('../src/utils/money.ts', import.meta.url), 'utf8'))
const entryUtils = run(await readFile(new URL('../src/utils/entry.ts', import.meta.url), 'utf8'), { './money': money })
const deferred = () => { let resolve; let reject; const promise = new Promise((yes, no) => { resolve = yes; reject = no }); return { promise, resolve, reject } }
async function entryPage(ledger, request = async () => ({})) {
  const hooks = {}
  const events = { toasts: [], tabSwitches: [] }
  const uni = { ...defaultUni, showToast: options => events.toasts.push(options), switchTab: options => events.tabSwitches.push(options) }
  const source = (await readFile(new URL('../src/pages/entry/entry.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { initialize, save, setType, back, closeDiscard, confirmDiscard, openModal, selectAccount, confirmModal, formatPickerDate, isSameAccountSelection, modal, discardOpen, draftDate, draftTime, draftNote, draftAccountIndex, accountIndex, toIndex, amount, expression, note, idempotencyKey, loading, loadError, editingId, routeId, dateTime, saving, type, typeLabel, isEdit };', {
    '@dcloudio/uni-app': { onLoad: fn => { hooks.load = fn }, onBackPress() {} },
    '../../components/CenterModal.vue': {}, '../../components/EntryDateTimePicker.vue': {}, '../../components/MoneyDisplay.vue': {}, '../../stores/ledger': { useLedger: () => ledger },
    '../../utils/api': { request }, '../../utils/money': money, '../../utils/entry': entryUtils,
    '../../utils/id': { stringId: value => value == null ? '' : String(value) },
  }, uni)
  return { ...page, hooks, events }
}
const account = { id: '10', kind: 'FUND', status: 'ACTIVE', name: '测试账户' }

test('编辑记账沿用原账单类型且不能切换顶部类型', async () => {
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {} }, async () => ({
    refundedCents: 0,
    transaction: { type: 'INCOME', originalAmountCents: 1234, accountId: '10', occurredAt: '2026-09-09T12:30:00', note: '工资' },
  }))
  page.routeId.value = '20'
  page.editingId.value = '20'
  await page.initialize()
  assert.equal(page.isEdit.value, true)
  assert.equal(page.type.value, 'INCOME')
  assert.equal(page.typeLabel.value, '收入')
  page.setType('EXPENSE')
  assert.equal(page.type.value, 'INCOME')
})

test('编辑页放弃修改使用自定义确认弹窗', async () => {
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {} })
  await page.initialize()
  page.note.value = '未保存的修改'
  page.back()
  assert.equal(page.discardOpen.value, true)
  page.closeDiscard()
  assert.equal(page.discardOpen.value, false)
  page.back()
  page.confirmDiscard()
  assert.equal(page.discardOpen.value, false)
})

test('加载失败的编辑页不允许降级为新增提交', async () => {
  let writes = 0
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => { throw new Error('网络失败') }, createTransaction: async () => { writes++ } })
  page.routeId.value = '20'; page.editingId.value = '20'
  await page.initialize(); page.amount.value = '1000'
  await page.save()
  assert.equal(writes, 0)
  assert.equal(page.loadError.value, '网络失败')
})
test('保存期间与成功返回前连续点击只写入一次', async () => {
  const pending = deferred(); const payloads = []
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {}, createTransaction: async payload => { payloads.push(payload); await pending.promise } })
  await page.initialize(); page.expression.value = '1000.01'
  const saving = page.save('home'); await page.save('home')
  assert.equal(payloads.length, 1); assert.equal(payloads[0].amountCents, 100001)
  pending.resolve(); await saving; await page.save('home')
  assert.equal(payloads.length, 1)
})
test('写入失败后保留金额并使用原幂等键重试', async () => {
  const payloads = []
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {}, createTransaction: async payload => { payloads.push(payload); throw new Error('超时') } })
  await page.initialize(); page.expression.value = '8500'
  await page.save('home'); await page.save('home')
  assert.equal(page.amount.value, '8500'); assert.equal(page.saving.value, false)
  assert.equal(payloads.length, 2); assert.equal(payloads[0].idempotencyKey, payloads[1].idempotencyKey)
})
test('点击账户后立即应用选择并关闭账户弹窗', async () => {
  const secondAccount = { ...account, id: '11', name: '备用账户' }
  const page = await entryPage({ state: { accounts: [account, secondAccount] }, refresh: async () => {} })
  await page.initialize()
  page.openModal('account'); page.selectAccount(1)
  assert.equal(page.accountIndex.value, 1); assert.equal(page.modal.value, '')
  page.openModal('to'); page.selectAccount(0)
  assert.equal(page.toIndex.value, 0); assert.equal(page.modal.value, '')
})
test('转账的转出和转入账户不能选择同一账户', async () => {
  const secondAccount = { ...account, id: '11', name: '备用账户' }
  const page = await entryPage({ state: { accounts: [account, secondAccount] }, refresh: async () => {} })
  await page.initialize(); page.setType('TRANSFER')
  page.openModal('to')
  assert.equal(page.isSameAccountSelection(0), true)
  page.selectAccount(0)
  assert.equal(page.toIndex.value, 1); assert.equal(page.modal.value, 'to')
  page.modal.value = ''
  page.openModal('account')
  assert.equal(page.isSameAccountSelection(1), true)
  page.selectAccount(1)
  assert.equal(page.accountIndex.value, 0); assert.equal(page.modal.value, 'account')
})
test('日期与时间弹窗在确认前不写回，并使用中文日期展示', async () => {
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {} })
  await page.initialize(); const original = page.dateTime.value
  page.openModal('date'); page.draftDate.value = '2026-09-09'; page.draftTime.value = '22:03'
  assert.equal(page.dateTime.value, original); assert.equal(page.formatPickerDate(page.draftDate.value), '2026年09月09日')
  page.confirmModal()
  assert.equal(page.dateTime.value, '2026-09-09T22:03:00'); assert.equal(page.modal.value, '')
})
test('备注弹窗在完成前不写回，完成后更新备注', async () => {
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {} })
  await page.initialize(); page.openModal('note'); page.draftNote.value = '和朋友聚餐'
  assert.equal(page.note.value, '')
  page.confirmModal()
  assert.equal(page.note.value, '和朋友聚餐'); assert.equal(page.modal.value, '')
})
test('确定保存后提示成功并跳转首页，再记保存后保留表单上下文供下一笔录入', async () => {
  const payloads = []
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {}, createTransaction: async payload => { payloads.push(payload) } })
  await page.initialize()
  page.expression.value = '2+3×4'; page.note.value = '午餐'
  await page.save('again')
  assert.equal(payloads[0].amountCents, 1400)
  assert.equal(page.expression.value, ''); assert.equal(page.amount.value, ''); assert.equal(page.note.value, '')
  assert.equal(page.events.toasts.length, 0); assert.equal(page.events.tabSwitches.length, 0)
  const firstKey = payloads[0].idempotencyKey
  page.expression.value = '5'; await page.save('again')
  assert.notEqual(payloads[1].idempotencyKey, firstKey)
  page.expression.value = '8'; await page.save('home')
  assert.deepEqual(page.events.tabSwitches.at(-1), { url: '/pages/index/index' })
})
test('日历入口日期被用于新增账单且不改变账户默认逻辑', async () => {
  const page = await entryPage({ state: { accounts: [account] }, refresh: async () => {} })
  page.hooks.load({ date: '2026-09-14' })
  assert.equal(page.dateTime.value.slice(0, 10), '2026-09-14')
})

test('日历快速切换日期时旧响应不能覆盖新选择', async () => {
  const requests = []
  const source = (await readFile(new URL('../src/pages/calendar/calendar.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { loadDay, selected, detail, dayLoading };', {
    '@dcloudio/uni-app': { onShow() {}, onPullDownRefresh() {} },
    '../../stores/ledger': { useLedger: () => ({ state: { token: 'test-session' } }) },
    '../../utils/api': { request: () => { const pending = deferred(); requests.push(pending); return pending.promise } },
    '../../utils/money': money,
  })
  page.selected.value = '2026-09-14'; const first = page.loadDay(page.selected.value)
  page.selected.value = '2026-09-15'; const second = page.loadDay(page.selected.value)
  assert.equal(page.detail.value, null); assert.equal(page.dayLoading.value, true)
  requests[1].resolve({ date: '2026-09-15', transactions: [] }); await second
  requests[0].resolve({ date: '2026-09-14', transactions: [] }); await first
  assert.equal(page.detail.value.date, '2026-09-15'); assert.equal(page.dayLoading.value, false)
})

test('账单写入成功后刷新失败不作为写入失败抛出', async () => {
  let writes = 0
  const ledgerModule = run(await readFile(new URL('../src/stores/ledger.ts', import.meta.url), 'utf8'), {
    '../utils/api': { request: async (url, options) => { if (options?.method === 'POST') { writes++; return {} } throw new Error('刷新失败') } },
    '../utils/money': money,
  })
  await ledgerModule.useLedger().createTransaction({ type: 'EXPENSE', amountCents: 100, accountId: '10', occurredAt: '2026-09-08T12:00' })
  assert.equal(writes, 1)
})

test('退款删除成功后立即移除记录，详情刷新失败也不回显旧记录', async () => {
  const hooks = {}
  const calls = []
  const ledger = { state: { accounts: [] }, refresh: async () => {} }
  const source = (await readFile(new URL('../src/pages/detail/detail.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { detail, id, loading, deleteOpen, saving, error, openDelete, confirmDelete };', {
    '@dcloudio/uni-app': { onLoad: fn => { hooks.load = fn }, onShow: fn => { hooks.show = fn } },
    '../../components/MoneyDisplay.vue': {}, '../../stores/ledger': { useLedger: () => ledger },
    '../../utils/api': { request: async (url, options) => {
      calls.push({ url, method: options?.method || 'GET' })
      if (options?.method === 'DELETE') return undefined
      throw new Error('详情刷新失败')
    } },
    '../../utils/money': money, '../../utils/entry': entryUtils,
    '../../utils/id': { stringId: value => value == null ? '' : String(value) },
  })
  page.id.value = '20'
  page.detail.value = {
    transaction: { id: '20', type: 'EXPENSE', amountCents: 750, originalAmountCents: 1000, hasRefund: true, accountId: '10', occurredAt: '2026-09-09T12:30:00', transactionNo: 'TRX-20', status: 'ACTIVE' },
    refundedCents: 250,
    effectiveCents: 750,
    refunds: [{ id: '31', refundNo: 'REF-31', amountCents: 250, createdAt: '2026-09-09T12:31:00' }],
  }
  page.loading.value = false
  page.openDelete({ type: 'refund', id: '31' })
  await page.confirmDelete()
  assert.deepEqual(calls.slice(0, 1), [{ url: '/api/app/transactions/refunds/31', method: 'DELETE' }])
  assert.equal(page.detail.value.refunds.length, 0)
  assert.equal(page.detail.value.refundedCents, 0)
  assert.equal(page.detail.value.effectiveCents, 1000)
  assert.equal(page.detail.value.transaction.amountCents, 1000)
  assert.equal(page.detail.value.transaction.hasRefund, false)
  assert.equal(page.deleteOpen.value, false)
  assert.equal(page.saving.value, false)
})

test('账单详情首次加载等待路由参数，避免 onShow 先发空 ID 请求', async () => {
  const hooks = {}
  const calls = []
  const detail = {
    transaction: { id: '20', type: 'EXPENSE', amountCents: 1000, originalAmountCents: 1000, hasRefund: false, accountId: '10', occurredAt: '2026-09-09T12:30:00', transactionNo: 'TRX-20' },
    refundedCents: 0,
    effectiveCents: 1000,
    refunds: [],
  }
  const source = (await readFile(new URL('../src/pages/detail/detail.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { id, detail, loading };', {
    '@dcloudio/uni-app': { onLoad: fn => { hooks.load = fn }, onShow: fn => { hooks.show = fn } },
    '../../components/MoneyDisplay.vue': {}, '../../stores/ledger': { useLedger: () => ({ state: { accounts: [] }, refresh: async () => {} }) },
    '../../utils/api': { request: async url => { calls.push(url); return detail } },
    '../../utils/money': money, '../../utils/entry': entryUtils,
    '../../utils/id': { stringId: value => value == null ? '' : String(value) },
  })
  hooks.show()
  assert.deepEqual(calls, [])
  hooks.load({ id: '20' })
  assert.deepEqual(calls, ['/api/app/transactions/20'])
  assert.equal(page.id.value, '20')
})

test('H5 启动不预取首页数据，由首页 onShow 唯一触发首次刷新', async () => {
  const appSource = await readFile(new URL('../src/App.vue', import.meta.url), 'utf8')
  const h5Startup = appSource.match(/\/\/ #ifdef H5([\s\S]*?)\/\/ #endif/)[1]
  assert.doesNotMatch(h5Startup, /ledger\.refresh\(/)

  const homeSource = await readFile(new URL('../src/pages/index/index.vue', import.meta.url), 'utf8')
  assert.match(homeSource, /onShow\(async \(\) => \{[\s\S]*?await load\(\)/)
  assert.match(homeSource, /async function load\(force = false\)[\s\S]*?state\.summary\?\.month === month\.value/)
  assert.match(homeSource, /async function load\(force = false\)[\s\S]*?ledger\.refresh\(month\.value\)/)
  assert.match(homeSource, /async function load\(force = false\)[\s\S]*?ledger\.loadHomeRecentTransactions\(\)/)
})

test('同月并发刷新合并账户与摘要请求', async () => {
  const requests = []
  const ledgerModule = run(await readFile(new URL('../src/stores/ledger.ts', import.meta.url), 'utf8'), {
    '../utils/api': { request: url => {
      const pending = deferred()
      requests.push({ url, pending })
      return pending.promise
    } },
    '../utils/money': money,
  })
  const ledger = ledgerModule.useLedger()
  const first = ledger.refresh('2026-09')
  const second = ledger.refresh('2026-09')
  assert.equal(requests.length, 2)
  requests.find(item => item.url.includes('/home/summary'))?.pending.resolve({ month: '2026-09', transactions: [] })
  requests.find(item => item.url.endsWith('/accounts'))?.pending.resolve([])
  const [firstSummary, secondSummary] = await Promise.all([first, second])
  assert.equal(firstSummary.month, '2026-09')
  assert.equal(secondSummary.month, '2026-09')
})

test('首页最近记账首次加载当月和上月，并以月份游标追加更早记录', async () => {
  const recentCalls = []
  const summary = { month: '2026-09', dailyExpenseCents: 0, expenseCents: 0, incomeCents: 0, balanceCents: 0, transactions: [] }
  const first = { id: '1', type: 'EXPENSE', amountCents: 100, occurredAt: '2026-09-10T12:00:00' }
  const second = { id: '2', type: 'INCOME', amountCents: 200, occurredAt: '2026-07-10T12:00:00' }
  const source = (await readFile(new URL('../src/pages/index/index.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { load, loadMore, recentTransactions, recentStartMonth, hasMore, loadingMore };', {
    '@dcloudio/uni-app': { onShow() {}, onPullDownRefresh() {} },
    '../../components/BottomNav.vue': {}, '../../components/PageHeader.vue': {}, '../../components/TransactionRow.vue': {}, '../../components/MoneyDisplay.vue': {},
    '../../stores/ledger': { useLedger: () => ({ state: { token: 'test-session' }, refresh: async () => summary, loadHomeRecentTransactions: async beforeMonth => {
      recentCalls.push(beforeMonth)
      return beforeMonth
        ? { startMonth: '2026-06', endMonth: '2026-07', transactions: [first, second], hasMore: false }
        : { startMonth: '2026-08', endMonth: '2026-09', transactions: [first], hasMore: true }
    } }) },
    '../../utils/money': money,
  })
  await page.load()
  assert.deepEqual(recentCalls, [undefined])
  assert.equal(page.recentStartMonth.value, '2026-08')
  assert.equal(page.hasMore.value, true)
  await page.loadMore()
  assert.deepEqual(recentCalls, [undefined, '2026-08'])
  assert.deepEqual(page.recentTransactions.value.map(item => item.id), ['1', '2'])
  assert.equal(page.hasMore.value, false)
  assert.equal(page.loadingMore.value, false)
})

test('我的页退出登录使用系统确认弹层并阻止重复提交', async () => {
  const source = (await readFile(new URL('../src/pages/mine/mine.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const pending = deferred()
  let logoutCalls = 0
  const events = { toasts: [] }
  const page = run(source + '\nexport { openLogout, closeLogout, confirmLogout, logoutOpen, loggingOut };', {
    '@dcloudio/uni-app': { onShow() {} },
    '../../components/BottomNav.vue': {}, '../../components/PageHeader.vue': {},
    '../../utils/file': { currentAvatar: async () => undefined, uploadAvatar: async () => ({}) },
    '../../utils/api': { request: async () => ({}) },
    '../../stores/ledger': { useLedger: () => ({ state: { token: 'test-session' }, logout: async () => { logoutCalls++; await pending.promise } }) },
  }, { ...defaultUni, showToast: options => events.toasts.push(options) })

  assert.doesNotMatch(source, /uni\.showModal/)
  assert.match(await readFile(new URL('../src/pages/mine/mine.vue', import.meta.url), 'utf8'), /class="asset-create-backdrop"/)
  page.openLogout()
  assert.equal(page.logoutOpen.value, true)
  const first = page.confirmLogout()
  await page.confirmLogout()
  assert.equal(logoutCalls, 1)
  assert.equal(page.loggingOut.value, true)
  pending.resolve()
  await first
  assert.equal(page.logoutOpen.value, false)
  assert.equal(page.loggingOut.value, false)
  assert.deepEqual(events.toasts, [{ title: '已退出登录', icon: 'none' }])
})

test('我的页三项设置使用相同的按钮行，关于帮助和退出登录保持既有入口', async () => {
  const source = await readFile(new URL('../src/pages/mine/mine.vue', import.meta.url), 'utf8')
  const profileCenter = source.match(/<button class="setting-item" aria-label="个人中心" @click="openProfile">([\s\S]*?)<\/button>/)?.[0]

  assert.ok(profileCenter)
  assert.match(profileCenter, /class="setting-icon setting-icon-profile">个<\/text>/)
  const styles = await readFile(new URL('../src/prototype.scss', import.meta.url), 'utf8')
  assert.match(styles, /\.setting-icon-profile \{ font-size:18px; \}/)
  assert.match(source, /<button class="setting-item" @click="openHelp">/)
  assert.match(source, /<button v-if="loggedIn" class="setting-item logout-item" :disabled="loggingOut" @click="openLogout">/)
})

test('我的页顶部头像为纯展示，不跳转、预览或触发更换照片', async () => {
  const source = await readFile(new URL('../src/pages/mine/mine.vue', import.meta.url), 'utf8')
  const avatar = source.match(/<view class="profile-avatar-button"[^>]*>[\s\S]*?<\/view>/)?.[0]

  assert.ok(avatar)
  assert.doesNotMatch(avatar, /@click|role=|aria-label=/)
  assert.doesNotMatch(source, /previewImage|previewAvatar|handleAvatarClick|uploadAvatar|chooseAvatar/)
  assert.match(source, /<button class="setting-item" aria-label="个人中心" @click="openProfile">/)
})

test('个人中心首次设置密码随资料保存提交，展示成功弹层 0.5 秒后自动返回我的页', async () => {
  const hooks = {}
  const requests = []
  const tabSwitches = []
  const toasts = []
  const ledger = { state: { token: 'test-session', user: undefined } }
  const fullSource = await readFile(new URL('../src/pages/profile/profile.vue', import.meta.url), 'utf8')
  const source = fullSource.match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { loadProfile, openPassword, confirmPassword, saveProfile, nickname, loginAccount, savedAccount, firstPassword, passwordConfigured, passwordOpen, accountLocked };', {
    '@dcloudio/uni-app': { onLoad: fn => { hooks.load = fn } },
    '../../stores/ledger': { useLedger: () => ledger },
    '../../utils/file': { currentAvatar: async () => null, uploadAvatar: async () => ({}) },
    '../../utils/api': { request: async (url, options = {}) => {
      requests.push({ url, options })
      if (url === '/api/app/user/profile' && !options.method) return { userId: '7', nickname: '账本主人', loginAccount: '', passwordConfigured: false, avatarAuthorized: true }
      return { userId: '7', nickname: '新昵称', loginAccount: 'firstuser', passwordConfigured: true, avatarAuthorized: true }
    } },
    '../../utils/passwordCrypto': { encryptPassword: async value => `encrypted:${value}` },
    '../../utils/entry': entryUtils,
  }, { ...defaultUni, showToast: options => toasts.push(options), reLaunch() {}, switchTab: options => tabSwitches.push(options) })

  await hooks.load()
  page.nickname.value = '新昵称'
  page.loginAccount.value = ' FirstUser '
  page.firstPassword.value = 'password-8'
  await page.saveProfile()
  assert.deepEqual(requests[1], { url: '/api/app/user/profile', options: { method: 'PUT', data: { nickname: '新昵称', loginAccount: 'firstuser', encryptedPassword: 'encrypted:password-8' } } })
  assert.equal(page.passwordConfigured.value, true)
  assert.equal(page.accountLocked.value, true)
  assert.equal(page.passwordOpen.value, false)
  assert.deepEqual(ledger.state.user, { id: '7', nickname: '新昵称' })
  assert.deepEqual(tabSwitches, [{ url: '/pages/mine/mine' }])
  assert.deepEqual(toasts, [])
  assert.match(source, /saveSuccessOpen/)
  assert.match(fullSource, /个人资料已更新，正在返回我的…/)
  assert.match(source, /}, 500\)/)
})

test('个人中心新头像上传后立即本地预览，点击保存才关联为当前头像', async () => {
  const requests = []
  const source = (await readFile(new URL('../src/pages/profile/profile.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { chooseAvatar, saveProfile, avatarUrl, pendingAvatar, avatarConfigured, nickname, loginAccount, passwordConfigured, saving };', {
    '@dcloudio/uni-app': { onLoad() {} },
    '../../stores/ledger': { useLedger: () => ({ state: { token: 'test-session' } }) },
    '../../utils/file': { uploadAvatar: async () => ({ fileId: '18', viewUrl: 'new-avatar-preview', objectKey: 'avatars/7/new.jpg' }), currentAvatar: async () => null },
    '../../utils/api': { request: async (url, options = {}) => {
      requests.push({ url, options })
      return { userId: '7', nickname: '新昵称', loginAccount: 'fixedaccount', passwordConfigured: true, avatarAuthorized: true, avatarFileUrl: 'avatars/7/new.jpg' }
    } },
    '../../utils/passwordCrypto': { encryptPassword: async value => `encrypted:${value}` },
    '../../utils/entry': entryUtils,
  }, { ...defaultUni, showToast() {} })
  const target = { files: [{}], value: 'selected-file' }

  await page.chooseAvatar({ target })

  assert.equal(page.avatarUrl.value, 'new-avatar-preview')
  assert.equal(page.avatarConfigured.value, false)
  assert.equal(page.pendingAvatar.value.fileId, '18')
  assert.equal(requests.length, 0)
  page.nickname.value = '新昵称'
  page.loginAccount.value = 'fixedaccount'
  page.passwordConfigured.value = true
  await page.saveProfile()

  assert.equal(requests[0].options.data.avatarFileId, '18')
  assert.equal(page.avatarUrl.value, 'new-avatar-preview')
  assert.equal(page.pendingAvatar.value, null)
  assert.equal(page.saving.value, false)
})

test('个人中心返回始终切换到我的页', async () => {
  const tabSwitches = []
  const source = (await readFile(new URL('../src/pages/profile/profile.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  const page = run(source + '\nexport { backToMine };', {
    '@dcloudio/uni-app': { onLoad() {} },
    '../../stores/ledger': { useLedger: () => ({ state: { token: 'test-session' } }) },
    '../../utils/file': {}, '../../utils/api': {}, '../../utils/passwordCrypto': {},
  }, { ...defaultUni, switchTab: options => tabSwitches.push(options) })

  page.backToMine()

  assert.deepEqual(tabSwitches, [{ url: '/pages/mine/mine' }])
  assert.doesNotMatch(source, /backToLedger/)
})

test('相同头像命中 READY 时携带摘要并直接复用预览，不重复 PUT', async () => {
  const calls = []
  const source = await readFile(new URL('../src/utils/file.ts', import.meta.url), 'utf8')
  const fileUtils = run(source, { './api': { request: async (url, options) => {
    calls.push({ url, options })
    if (url === '/api/app/files/upload-url') return { fileId: '18', uploadUrl: '', expiresInSeconds: 600, status: 'READY' }
    return { fileId: '18', viewUrl: 'temporary-view-url', expiresInSeconds: 600, objectKey: 'avatars/7/existing.png' }
  } } })
  const file = {
    name: 'avatar.png', type: 'image/png', size: 8,
    arrayBuffer: async () => new Uint8Array([137, 80, 78, 71, 13, 10, 26, 10]).buffer,
    slice: () => ({ arrayBuffer: async () => new Uint8Array([137, 80, 78, 71, 13, 10, 26, 10]).buffer }),
  }

  const result = await fileUtils.uploadAvatar(file)

  assert.equal(calls.length, 2)
  assert.match(calls[0].options.data.fileHash, /^[0-9a-f]{64}$/)
  assert.deepEqual(calls.map(call => call.url), ['/api/app/files/upload-url', '/api/app/files/18/view-url'])
  assert.equal(result.objectKey, 'avatars/7/existing.png')
})

test('头像扩展名或浏览器 MIME 错误时，以真实图片头的类型上传', async () => {
  const calls = []
  const source = await readFile(new URL('../src/utils/file.ts', import.meta.url), 'utf8')
  const fileUtils = run(source, { './api': { request: async (url, options) => {
    calls.push({ url, options })
    if (url === '/api/app/files/upload-url') return { fileId: '19', uploadUrl: '', expiresInSeconds: 600, status: 'READY' }
    return { fileId: '19', viewUrl: 'temporary-view-url', expiresInSeconds: 600, objectKey: 'avatars/7/existing.jpg' }
  } } })
  const jpeg = new Uint8Array([0xff, 0xd8, 0xff, 0xe0, 0, 0, 0, 0, 0, 0, 0, 0]).buffer
  const file = {
    name: 'cat.png', type: 'image/png', size: 12,
    arrayBuffer: async () => jpeg,
    slice: () => ({ arrayBuffer: async () => jpeg }),
  }

  await fileUtils.uploadAvatar(file)

  assert.equal(calls[0].options.data.contentType, 'image/jpeg')
})

test('个人中心头像按钮动态创建原生 H5 文件输入框', async () => {
  const source = (await readFile(new URL('../src/pages/profile/profile.vue', import.meta.url), 'utf8')).match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
  let clicks = 0
  const picker = { type: '', accept: '', value: 'old', style: {}, addEventListener() {}, click: () => { clicks++ }, remove() {} }
  const previousDocument = globalThis.document
  Object.defineProperty(globalThis, 'document', { configurable: true, value: { createElement: tag => {
    assert.equal(tag, 'input')
    return picker
  }, body: { appendChild: input => assert.equal(input, picker) } } })
  try {
    const page = run(source + '\nexport { handleAvatarClick, avatarFileInput, uploading, saving };', {
      '@dcloudio/uni-app': { onLoad() {} },
      '../../stores/ledger': { useLedger: () => ({ state: { token: 'test-session' } }) },
      '../../utils/file': {}, '../../utils/api': {}, '../../utils/passwordCrypto': {}, '../../utils/entry': {},
    })
    page.handleAvatarClick()
    page.handleAvatarClick()

    assert.equal(picker.type, 'file')
    assert.equal(picker.accept, 'image/jpeg,image/png,image/webp,image/gif')
    assert.equal(picker.value, '')
    assert.equal(clicks, 2)
    assert.equal(page.avatarFileInput.value.type, 'file')
  } finally {
    Object.defineProperty(globalThis, 'document', { configurable: true, value: previousDocument })
  }
  assert.match(source, /document\.createElement\('input'\)/)
  assert.doesNotMatch(source, /profile-avatar-file-input/)
})

test('登录和注册均须先同意两份协议，且协议页可在未登录状态访问', async () => {
  const source = await readFile(new URL('../src/components/AuthPage.vue', import.meta.url), 'utf8')
  const guard = run(await readFile(new URL('../src/utils/h5AuthGuard.ts', import.meta.url), 'utf8'))
  const pages = JSON.parse(await readFile(new URL('../src/pages.json', import.meta.url), 'utf8'))
  const legalDocuments = await readFile(new URL('../src/constants/legalDocuments.ts', import.meta.url), 'utf8')

  assert.match(source, /const agreementAccepted = ref\(false\)/)
  assert.match(source, /agreementAccepted\.value/)
  assert.match(source, /请先阅读并同意用户协议和隐私协议/)
  assert.match(source, /openLegalDocument\('agreement'\)/)
  assert.match(source, /openLegalDocument\('privacy'\)/)
  assert.equal(guard.isH5AuthPath('/pages/legal/agreement/agreement'), true)
  assert.equal(guard.isH5AuthPath('/pages/legal/privacy/privacy'), true)
  assert.ok(pages.pages.some(page => page.path === 'pages/legal/agreement/agreement'))
  assert.ok(pages.pages.some(page => page.path === 'pages/legal/privacy/privacy'))
  assert.match(legalDocuments, /账号与认证信息/)
  assert.match(legalDocuments, /安全与运行信息/)
  assert.match(legalDocuments, /不会出售你的个人信息/)
})

test('认证与个人中心账号仅保留英文字母和数字', async () => {
  const authSource = await readFile(new URL('../src/components/AuthPage.vue', import.meta.url), 'utf8')
  const profileSource = await readFile(new URL('../src/pages/profile/profile.vue', import.meta.url), 'utf8')

  assert.match(authSource, /\^\[a-zA-Z0-9\]\{2,64\}\$/)
  assert.match(authSource, /replace\(\/\[\^a-zA-Z0-9\]\/g, ''\)/)
  assert.match(profileSource, /\^\[a-z0-9\]\{2,64\}\$/)
  assert.match(profileSource, /replace\(\/\[\^a-zA-Z0-9\]\/g, ''\)/)
  assert.match(profileSource, /仅支持 2-64 位英文字母或数字/)
})

test('认证页使用独立认证密码载荷，个人中心仍维持 RSA 密文密码字段', async () => {
  const authSource = await readFile(new URL('../src/components/AuthPage.vue', import.meta.url), 'utf8')
  const cryptoSource = await readFile(new URL('../src/utils/passwordCrypto.ts', import.meta.url), 'utf8')
  const profileSource = await readFile(new URL('../src/pages/profile/profile.vue', import.meta.url), 'utf8')

  assert.match(authSource, /encryptH5AuthPassword/)
  assert.match(authSource, /\.\.\.await encryptH5AuthPassword\(password\.value\)/)
  assert.match(cryptoSource, /compatibilityPassword: encodeHttpCompatibilityPassword\(password\)/)
  assert.match(cryptoSource, /insecurePasswordAllowed/)
  assert.doesNotMatch(cryptoSource, /plainPassword/)
  assert.match(profileSource, /encryptPassword\(newPassword\.value\)/)
})
