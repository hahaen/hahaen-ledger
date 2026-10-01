import { readFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'
const require = createRequire(import.meta.url)
const vue = require('vue')
const { compile } = createRequire(require.resolve('vue'))('@vue/compiler-dom')
const source = await readFile(new URL('../src/pages/mine/mine.vue', import.meta.url), 'utf8')
const script = source.match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
const card = source.match(/<view class="profile-card">([\s\S]*?)\n    <view class="days-card">/)[0].replace(/\n    <view class="days-card">$/, '')
const render = new Function('Vue', compile(card, { isCustomElement: () => true }).code)(vue)
const deferred = () => { let resolve, reject; const promise = new Promise((yes, no) => { resolve = yes; reject = no }); return { promise, resolve, reject } }
const data = { userId: '1', nickname: '主人', cumulativeDays: 10, avatarAuthorized: true, avatarFileUrl: 'avatar/one' }
const avatar = { viewUrl: '/fixture-avatar-one.png', expiresInSeconds: 300 }
async function page() {
  const state = vue.reactive({ token: 'session-one', user: { id: '1', nickname: '主人' } })
  const api = { profile: async () => ({ ...data }), avatar: async () => ({ ...avatar }) }
  const hooks = {}, exports = {}; let avatarCalls = 0
  const deps = { vue, '@dcloudio/uni-app': { onShow: fn => { hooks.show = fn } }, '../../utils/api': { request: () => api.profile() }, '../../utils/file': { currentAvatar: () => { avatarCalls++; return api.avatar() } }, '../../stores/ledger': { useLedger: () => ({ state, logout: async () => { state.token = '' } }) }, '../../utils/staticResource': { staticResource: () => '/default.png' }, '../../utils/wechatShare': { registerWechatShare() {} } }
  const js = ts.transpileModule(script + '\nexport { loadProfile, profile, avatarUrl, loading, loggedIn, nickname, avatarStatus, openLogin, confirmLogout };', { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ESNext } }).outputText
  new Function('require', 'exports', 'uni', js)(name => deps[name] || {}, exports, { getStorageSync: () => state.token, showToast() {}, reLaunch() {} })
  const snapshot = () => {
    const vnode = render(vue.proxyRefs(exports), [])
    const walk = node => typeof node === 'string' ? node : [node?.props?.src || '', ...(Array.isArray(node?.children) ? node.children.map(walk) : [typeof node?.children === 'string' ? node.children : ''])].join('|')
    return walk(vnode)
  }
  return { ...exports, api, state, hooks, snapshot, avatarCalls: () => avatarCalls }
}
test('切回我的：慢资料和头像请求期间已显示头像与状态文案保持不变', async () => {
  const p = await page(); await p.loadProfile(); const before = p.snapshot()
  const pending = deferred(); p.api.profile = () => pending.promise
  p.hooks.show(); assert.equal(p.snapshot(), before)
  pending.resolve({ ...data }); await new Promise(resolve => setImmediate(resolve))
  assert.equal(p.snapshot(), before); assert.equal(p.avatarCalls(), 1)
})
test('相同头像有效期内复用地址，不反复更新图片src', async () => {
  const p = await page(); await p.loadProfile()
  await p.loadProfile(); await p.loadProfile()
  assert.equal(p.avatarCalls(), 1); assert.equal(p.avatarUrl.value, avatar.viewUrl)
})
test('头像变更：等待新预览期间保留旧图，成功后替换；移除时回默认图', async () => {
  const p = await page(); await p.loadProfile()
  const pending = deferred(); p.api.profile = async () => ({ ...data, avatarFileUrl: 'avatar/two' }); p.api.avatar = () => pending.promise
  const refresh = p.loadProfile(); await Promise.resolve()
  assert.equal(p.avatarUrl.value, avatar.viewUrl)
  pending.resolve({ ...avatar, viewUrl: '/fixture-avatar-two.png' }); await refresh
  assert.equal(p.avatarUrl.value, '/fixture-avatar-two.png')
  p.api.profile = async () => ({ ...data, avatarFileUrl: undefined, avatarAuthorized: false }); await p.loadProfile()
  assert.equal(p.avatarUrl.value, '/default.png'); assert.equal(p.avatarStatus.value, '')
})
test('首次保留加载反馈；资料失败与预览失败不清掉已有内容，预览可重试', async () => {
  const p = await page(); const pending = deferred(); p.api.profile = () => pending.promise
  const first = p.loadProfile(); assert.match(p.snapshot(), /正在加载个人资料/)
  pending.resolve({ ...data }); await first; const before = p.snapshot()
  p.api.profile = async () => { throw new Error('offline') }; await p.loadProfile(); assert.equal(p.snapshot(), before)
  p.api.profile = async () => ({ ...data, avatarFileUrl: 'avatar/two' }); p.api.avatar = async () => { throw new Error('offline') }
  await p.loadProfile(); assert.equal(p.avatarUrl.value, avatar.viewUrl)
  p.api.avatar = async () => ({ ...avatar, viewUrl: '/fixture-avatar-two.png' }); await p.loadProfile(); assert.equal(p.avatarUrl.value, '/fixture-avatar-two.png')
})
test('过期头像重新获取预览，等待时保持旧图', async () => {
  const p = await page(); p.api.avatar = async () => ({ ...avatar, expiresInSeconds: 0 }); await p.loadProfile()
  const pending = deferred(); p.api.avatar = () => pending.promise
  const refresh = p.loadProfile(); await Promise.resolve(); assert.equal(p.avatarUrl.value, avatar.viewUrl)
  pending.resolve({ ...avatar, viewUrl: '/fixture-renewed.png' }); await refresh
  assert.equal(p.avatarUrl.value, '/fixture-renewed.png'); assert.equal(p.avatarCalls(), 2)
})
test('会话变化清理旧资料，过时响应不能覆盖新用户或退出状态', async () => {
  const p = await page(); await p.loadProfile(); const old = deferred(); p.api.profile = () => old.promise
  const refresh = p.loadProfile(); p.state.token = 'session-two'; const next = deferred(); p.api.profile = () => next.promise
  const other = p.loadProfile(); assert.equal(p.profile.value, null); assert.equal(p.avatarUrl.value, '/default.png')
  old.resolve({ ...data }); await refresh; assert.equal(p.profile.value, null)
  next.resolve({ ...data, userId: '2', nickname: '新用户', avatarFileUrl: undefined }); await other; assert.equal(p.nickname.value, '新用户')
  const stale = deferred(); p.api.profile = () => stale.promise; const last = p.loadProfile()
  p.state.token = ''; await p.loadProfile(); stale.resolve({ ...data }); await last
  assert.equal(p.profile.value, null); assert.equal(p.avatarUrl.value, '/default.png'); assert.equal(p.loading.value, false)
})
test('后到的旧头像预览不能覆盖新头像', async () => {
  const p = await page(); const old = deferred(); p.api.avatar = () => old.promise
  const first = p.loadProfile(); await Promise.resolve()
  p.api.profile = async () => ({ ...data, avatarFileUrl: 'avatar/two' }); p.api.avatar = async () => ({ ...avatar, viewUrl: '/fixture-avatar-two.png' })
  await p.loadProfile(); old.resolve({ ...avatar }); await first
  assert.equal(p.avatarUrl.value, '/fixture-avatar-two.png')
})
test('退出登录会使正在加载的头像响应失效', async () => {
  const p = await page(); const old = deferred(); p.api.avatar = () => old.promise
  const first = p.loadProfile(); await Promise.resolve(); await p.confirmLogout()
  old.resolve({ ...avatar }); await first
  assert.equal(p.profile.value, null); assert.equal(p.avatarUrl.value, '/default.png'); assert.equal(p.loading.value, false)
})
