import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'

const source = await readFile(new URL('../src/utils/wechatShare.ts', import.meta.url), 'utf8')
const pageFiles = [
  '../src/pages/index/index.vue',
  '../src/pages/calendar/calendar.vue',
  '../src/pages/assets/assets.vue',
  '../src/pages/mine/mine.vue',
  '../src/pages/detail/detail.vue',
  '../src/pages/account/account.vue',
  '../src/pages/help/help.vue',
  '../src/pages/profile/profile.vue',
]

test('微信转发内容不包含账单隐私且统一回到首页', () => {
  assert.match(source, /WECHAT_SHARE_TITLE\s*=\s*'哈记账｜简单记账，安心生活'/)
  assert.match(source, /WECHAT_SHARE_PATH\s*=\s*'\/pages\/index\/index'/)
  assert.match(source, /onShareAppMessage\(\(\) => createWechatShareMessage\(\)\)/)
})

for (const pageFile of pageFiles) {
  test(`${pageFile} 注册微信转发生命周期`, async () => {
    const pageSource = await readFile(new URL(pageFile, import.meta.url), 'utf8')
    assert.match(pageSource, /registerWechatShare\(\)/)
  })
}
