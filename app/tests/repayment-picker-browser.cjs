// 对实际 H5 页面发送触摸手势；所有 API 均拦截为合成数据，禁止真实账务写入。
const assert = require('node:assert/strict')
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright')

async function swipe(page, locator, upwards = true) {
  const box = await locator.boundingBox()
  assert.ok(box && box.height > 50, '列表必须有可滑动高度')
  const cdp = await page.context().newCDPSession(page)
  const x = box.x + box.width / 2
  const start = box.y + box.height * (upwards ? 0.85 : 0.15)
  const end = box.y + box.height * (upwards ? 0.15 : 0.85)
  try {
    await cdp.send('Input.dispatchTouchEvent', { type: 'touchStart', touchPoints: [{ x, y: start }] })
    for (let step = 1; step <= 12; step++) {
      await cdp.send('Input.dispatchTouchEvent', { type: 'touchMove', touchPoints: [{ x, y: start + (end - start) * step / 12 }] })
      await page.waitForTimeout(20)
    }
    await cdp.send('Input.dispatchTouchEvent', { type: 'touchEnd', touchPoints: [] })
    await page.waitForTimeout(250)
  } finally { await cdp.detach() }
}

;(async () => {
  const browser = await chromium.launch({ headless: true, executablePath: process.env.CHROME_EXECUTABLE_PATH })
  try {
    for (const [width, height] of [[375,667], [320,568], [667,375], [414,896]]) {
      const page = await browser.newPage({ viewport: { width, height }, isMobile: true, hasTouch: true })
      await page.addInitScript(() => localStorage.setItem('auth-token', 'synthetic-scroll-fixture'))
      const credit = { id: '100', name: '测试信用卡', kind: 'CREDIT', balanceCents: 12345, creditLimitCents: 500000, includedInNetAsset: true, status: 'ACTIVE', sortOrder: 100 }
      const funds = Array.from({ length: 12 }, (_, index) => ({ ...credit, id: String(index + 1), name: `资金账户${index + 1}`, kind: 'FUND', balanceCents: 27300, sortOrder: index }))
      let writes = 0
      await page.route('**/*', async route => {
        const url = new URL(route.request().url())
        if (url.pathname.includes('/api/app/')) {
          if (route.request().method() !== 'GET') { writes++; return route.fulfill({ status: 405, json: { code: 405 } }) }
          let data = null
          if (url.pathname.endsWith('/accounts/100')) data = credit
          else if (url.pathname.endsWith('/accounts')) data = [credit, ...funds]
          else if (url.pathname.endsWith('/transactions')) data = { items: [], total: 0, page: 1, pageSize: 50 }
          else if (url.pathname.endsWith('/home/summary')) data = { transactions: [], expenseCents: 0, incomeCents: 0 }
          return route.fulfill({ headers: { 'Access-Control-Allow-Origin': '*' }, json: { code: 0, data } })
        }
        // 仅本地页面资源可连接；远程图像不影响滚动验证。
        if (!['localhost', '127.0.0.1'].includes(url.hostname)) return route.abort()
        return route.continue()
      })
      await page.goto(`${process.env.PICKER_TEST_URL || 'http://127.0.0.1:5180/haji/'}#/pages/account/account?id=100`)
      await page.locator('.detail-action.refund').click()
      await page.locator('.repay-modal .repay-title').click()
      await page.locator('.repay-amount-money input').click()
      await page.locator('.repay-amount-money input').fill('12.34')
      assert.equal(await page.locator('.repay-modal').count(), 1, '点击并修改金额后还款弹窗不能关闭')
      await page.locator('.repay-account-picker').click()
      const list = page.locator('.repay-choice-list')
      await list.waitFor()
      const last = page.locator('.repay-choice-item').last()
      const before = await last.boundingBox()
      const background = await page.evaluate(() => ({ page: window.scrollY, records: document.querySelector('.account-record-list .uni-scroll-view')?.scrollTop || 0 }))
      await swipe(page, list)
      const after = await last.boundingBox()
      assert.ok(after.y < before.y - 20, `手指上滑后账户未移动: ${JSON.stringify({ width, height, beforeY: before.y, afterY: after.y })}`)
      await swipe(page, list, false)
      assert.ok((await last.boundingBox()).y > after.y + 20, '手指下滑应能返回前面的账户')
      for (let attempt = 0; attempt < 18; attempt++) {
        const target = await last.boundingBox(), viewport = await list.boundingBox()
        if (target.y >= viewport.y && target.y + target.height <= viewport.y + viewport.height + 1) break
        await swipe(page, list)
      }
      const target = await last.boundingBox(), viewport = await list.boundingBox()
      assert.ok(target.y >= viewport.y - 1 && target.y + target.height <= viewport.y + viewport.height + 1, '必须能滑到最后一个账户')
      const back = await page.locator('.repay-return').boundingBox()
      assert.ok(back.y >= 0 && back.y + back.height <= height, '返回还款按钮必须位于屏幕内')
      if (width === 375 && process.env.PICKER_SCREENSHOT) await page.screenshot({ path: process.env.PICKER_SCREENSHOT })
      await last.click()
      await page.locator('.repay-account-name').filter({ hasText: '资金账户12' }).waitFor()
      assert.equal(await page.locator('.repay-amount-money input').inputValue(), '12.34')
      await page.locator('.repay-account-picker').click()
      await page.getByText('返回还款', { exact: true }).click()
      assert.equal(await page.locator('.repay-amount-money input').inputValue(), '12.34')
      await page.locator('.repay-account-picker').click()
      await page.locator('.repay-account-modal .repay-title').click()
      assert.equal(await page.locator('.repay-account-modal').count(), 1, '点击内容不会误关闭')
      await page.locator('.repay-picker-backdrop > .picker-touch-mask').click({ position: { x: 2, y: 2 } })
      assert.equal(await page.locator('.repay-account-modal').count(), 0, '点击独立遮罩仍能关闭')
      await page.locator('.detail-action.refund').click()
      await page.locator('.repay-cancel').click()
      assert.equal(await page.locator('.repay-modal').count(), 0, '取消还款仍能关闭')
      await page.locator('.detail-action.refund').click()
      await page.locator('.repay-form-backdrop > .picker-touch-mask').click({ position: { x: 2, y: 2 } })
      assert.equal(await page.locator('.repay-modal').count(), 0, '点击主弹窗遮罩仍能关闭')
      assert.deepEqual(await page.evaluate(() => ({ page: window.scrollY, records: document.querySelector('.account-record-list .uni-scroll-view')?.scrollTop || 0 })), background)
      assert.equal(writes, 0)
      assert.equal(await page.evaluate(() => document.documentElement.scrollWidth), width)
      console.log(JSON.stringify({ viewport: [width, height], touchScroll: 'PASS', lastAccount: 'PASS', returnAndAmount: 'PASS', noBackgroundScroll: 'PASS' }))
      await page.close()
    }
  } finally { await browser.close() }
})().catch(error => { console.error(error); process.exitCode = 1 })
