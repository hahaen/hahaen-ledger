import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'

const source = await readFile(new URL('../src/utils/wechatShare.ts', import.meta.url), 'utf8')
const pagesJson = JSON.parse(await readFile(new URL('../src/pages.json', import.meta.url), 'utf8'))
const pageFiles = pagesJson.pages.map(({ path }) => `../src/${path}.vue`)

test('微信转发内容不包含账单隐私且统一回到首页', () => {
  assert.match(source, /WECHAT_SHARE_TITLE\s*=\s*'哈记账｜简单记账，安心生活'/)
  assert.match(source, /WECHAT_SHARE_PATH\s*=\s*'\/pages\/index\/index'/)
  assert.match(source, /onShareAppMessage\(\(\) => createWechatShareMessage\(\)\)/)
})

for (const pageFile of pageFiles) {
  test(`${pageFile} 注册统一微信转发生命周期`, async () => {
    const pageSource = await readFile(new URL(pageFile, import.meta.url), 'utf8')
    assert.match(pageSource, /registerWechatShare\(\)/)
  })
}
