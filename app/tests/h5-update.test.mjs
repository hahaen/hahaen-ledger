import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'

const source = await readFile(new URL('../src/utils/h5Update.ts', import.meta.url), 'utf8')
const compiled = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ESNext, module: ts.ModuleKind.ESNext } }).outputText
const { createH5UpdateGuard, parseH5Version, h5RefreshUrl, fetchH5Version } = await import(`data:text/javascript;base64,${Buffer.from(compiled).toString('base64')}`)

function fixture() {
  let response = { version: 'v1' }
  let reads = 0
  let prompts = 0
  const sources = []
  let visible = true
  let time = 0
  const navigations = []
  const guard = createH5UpdateGuard({
    currentVersion: 'v1',
    readVersion: async () => { reads++; if (response instanceof Error) throw response; return typeof response === 'function' ? response() : response },
    isVisible: () => visible,
    onRequired: source => { prompts++; sources.push(source) },
    navigate: version => navigations.push(version),
  }, () => time)
  return { guard, navigations, sources, reads: () => reads, prompts: () => prompts, respond: value => { response = value }, visible: value => { visible = value }, advance: () => { time += 5000 } }
}

test('手动入口同版本也能弹窗并刷新，重复触发不创建多个弹窗', async () => {
  const f = fixture()
  await f.guard.check()
  f.guard.requireRefresh()
  f.guard.requireRefresh()
  assert.equal(f.prompts(), 1)
  assert.deepEqual(f.sources, ['manual'])
  await f.guard.refresh()
  assert.deepEqual(f.navigations, ['v1'])
})

test('自动探测与手动入口交错时只保留一个弹窗和来源', async () => {
  const f = fixture()
  let resolve
  f.respond(() => new Promise(done => { resolve = done }))
  const checking = f.guard.check()
  f.guard.requireRefresh()
  resolve({ version: 'v2' })
  await checking
  assert.deepEqual(f.sources, ['manual'])
  f.respond({ version: 'v3' })
  await f.guard.refresh()
  assert.deepEqual(f.navigations, ['v3'])

  const automatic = fixture()
  automatic.respond({ version: 'v2' })
  await automatic.guard.check()
  automatic.guard.requireRefresh()
  assert.deepEqual(automatic.sources, ['update'])
})

test('一致版本不弹窗，无效响应/错误不锁定，后续可检测更新且仅弹一次', async () => {
  const f = fixture()
  for (const response of [{ version: 'v1' }, null, '<html>404</html>', { version: '' }, { version: '../index.html' }, new Error('offline')]) {
    f.respond(response)
    await f.guard.check()
    assert.equal(f.prompts(), 0)
    f.advance()
  }
  f.respond({ version: 'v2' })
  await f.guard.check()
  assert.equal(f.prompts(), 1)
  f.advance()
  await f.guard.check()
  assert.equal(f.prompts(), 1)
  assert.equal(f.reads(), 7)
})

test('隐藏页面暂停检测，回到前台检查；短时间触发与并发只发一次请求', async () => {
  const f = fixture()
  f.visible(false)
  await f.guard.check()
  assert.equal(f.reads(), 0)
  f.visible(true)
  let resolve
  f.respond(() => new Promise(done => { resolve = done }))
  const checks = [f.guard.check(), f.guard.check(), f.guard.check()]
  assert.equal(f.reads(), 1)
  resolve({ version: 'v1' })
  await Promise.all(checks)
  await f.guard.check()
  assert.equal(f.reads(), 1)
  f.advance()
  f.respond({ version: 'v2' })
  await f.guard.check()
  assert.equal(f.prompts(), 1)
})

test('无更新不能刷新；确认刷新重新取最新版本，连续发布不使用旧目标', async () => {
  const f = fixture()
  await f.guard.refresh()
  assert.equal(f.reads(), 0)
  f.respond({ version: 'v2' })
  await f.guard.check()
  f.respond({ version: 'v3' })
  await f.guard.refresh()
  assert.deepEqual(f.navigations, ['v3'])
  await f.guard.refresh()
  assert.equal(f.reads(), 2)
  assert.deepEqual(f.navigations, ['v3'])
})

test('刷新失败保留拦截并允许重试，重复点击只发一次刷新请求', async () => {
  const f = fixture()
  f.respond({ version: 'v2' })
  await f.guard.check()
  f.respond(new Error('offline'))
  await assert.rejects(f.guard.refresh(), /offline/)
  f.respond({ version: null })
  await assert.rejects(f.guard.refresh(), /无效/)
  assert.deepEqual(f.navigations, [])
  let resolve
  f.respond(() => new Promise(done => { resolve = done }))
  const refreshes = [f.guard.refresh(), f.guard.refresh()]
  assert.equal(f.reads(), 4)
  resolve({ version: 'v4' })
  await Promise.all(refreshes)
  assert.deepEqual(f.navigations, ['v4'])
  assert.equal(f.prompts(), 1)
})

test('刷新 URL 保留原路径、查询和 hash，替换旧缓存参数且不清空登录存储', () => {
  const url = new URL(h5RefreshUrl('https://example.com/haji/?source=pwa&__haji_v=old#/pages/detail/detail?id=42', 'v2', 123))
  assert.equal(url.pathname, '/haji/')
  assert.equal(url.searchParams.get('source'), 'pwa')
  assert.deepEqual(url.searchParams.getAll('__haji_v'), ['v2_123'])
  assert.equal(url.hash, '#/pages/detail/detail?id=42')
  assert.equal(parseH5Version({ version: 'a'.repeat(129) }), undefined)
  assert.equal(parseH5Version({ version: 'a-1_2' }), 'a-1_2')
})

test('版本请求禁用缓存，HTTP错误及非JSON不认为更新', async () => {
  const url = new URL('https://example.com/haji/haji-version.json?_=123')
  const result = await fetchH5Version(url, async (input, options) => {
    assert.equal(input, url)
    assert.equal(options.cache, 'no-store')
    assert.ok(options.signal instanceof AbortSignal)
    return new Response('{"version":"v2"}')
  })
  assert.equal(result.version, 'v2')
  await assert.rejects(fetchH5Version(url, async () => new Response('error', { status: 503 })))
  await assert.rejects(fetchH5Version(url, async () => new Response('<html>fallback</html>')))
})

test('挂起请求会超时中止，下一次检测可恢复', async () => {
  let aborted = false
  await assert.rejects(fetchH5Version(new URL('https://example.com/haji/haji-version.json'), async (_url, options) => new Promise((_resolve, reject) => {
    options.signal.addEventListener('abort', () => { aborted = true; reject(new Error('timeout')) })
  }), 5), /timeout/)
  assert.equal(aborted, true)
})
