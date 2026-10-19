// 真实 H5 生产页面 + 合成接口；全部业务请求均拦截，不连接真实账务。
const assert = require('node:assert/strict')
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright')

;(async () => {
  const browser = await chromium.launch({ headless: true, executablePath: process.env.CHROME_EXECUTABLE_PATH })
  try {
    for (const [width, height] of [[375,667], [320,568], [667,375], [414,896]]) {
      for (const type of ['EXPENSE', 'INCOME']) {
        const page = await browser.newPage({ viewport: { width, height }, isMobile: true, hasTouch: true })
        await page.addInitScript(() => localStorage.setItem('auth-token', 'synthetic-refund-fixture'))
        const transaction = { id: '100', transactionNo: 'SYNTHETIC-100', type, accountId: '1', amountCents: 1591800, originalAmountCents: 1591800, hasRefund: false, occurredAt: '2026-10-19T12:00:00', status: 'ACTIVE' }
        let refundedCents = 0, releaseFirst
        const writes = []
        await page.route('**/*', async route => {
          const url = new URL(route.request().url())
          const json = data => route.fulfill({ headers: { 'Access-Control-Allow-Origin': '*' }, json: { code: 0, data } })
          if (url.pathname.includes('/api/app/')) {
            if (url.pathname.endsWith('/transactions/100/refunds') && route.request().method() === 'POST') {
              writes.push(route.request().postDataJSON())
              if (writes.length === 1) {
                await new Promise(resolve => { releaseFirst = resolve })
                return route.fulfill({ status: 503, json: { code: 503, message: '合成退款失败，请重试' } })
              }
              refundedCents = writes.at(-1).amountCents
              return json({ id: '200', refundNo: 'SYNTHETIC-200', amountCents: refundedCents, createdAt: '2026-10-19T12:01:00' })
            }
            if (route.request().method() !== 'GET') return route.fulfill({ status: 405, json: { code: 405 } })
            if (url.pathname.endsWith('/transactions/100')) return json({ transaction: { ...transaction, amountCents: transaction.originalAmountCents - refundedCents, hasRefund: refundedCents > 0 }, refundedCents, effectiveCents: transaction.originalAmountCents - refundedCents, refunds: refundedCents ? [{ id: '200', refundNo: 'SYNTHETIC-200', amountCents: refundedCents, createdAt: '2026-10-19T12:01:00' }] : [] })
            if (url.pathname.endsWith('/accounts')) return json([{ id: '1', name: '合成资金账户', kind: 'FUND', status: 'ACTIVE', balanceCents: 2000000 }])
            if (url.pathname.endsWith('/home/summary')) return json({ transactions: [], expenseCents: 0, incomeCents: 0 })
            return json(null)
          }
          if (!['localhost', '127.0.0.1'].includes(url.hostname)) return route.abort()
          return route.continue()
        })
        await page.goto(`${process.env.REFUND_TEST_URL || 'http://127.0.0.1:18761/haji/'}#/pages/detail/detail?id=100`)
        const modal = page.locator('.refund-modal'), input = page.locator('.refund-input-wrap input')
        const fill = async value => {
          await input.fill(value)
          // 当前 uni-app H5 的 v-model input 事件节流为 100ms，等待同步后再提交。
          await page.waitForTimeout(150)
        }
        const open = () => page.locator('.detail-action.refund').click()
        const background = () => page.locator('.refund-backdrop > .picker-touch-mask').click({ position: { x: 2, y: 2 } })
        await open()
        assert.equal(await input.inputValue(), '15918')
        await page.locator('.refund-title').click()
        await input.click()
        await fill('12.34')
        assert.equal(await modal.count(), 1, '点击金额和输入不能关闭弹窗')
        await page.locator('.refund-cancel').click()
        assert.equal(await modal.count(), 0)
        await open()
        await background()
        assert.equal(await modal.count(), 0)
        await open()
        await fill('15918.01')
        await page.locator('.refund-confirm').click()
        await page.getByText('退款金额不能超过剩余可退款金额', { exact: true }).waitFor()
        assert.equal(writes.length, 0)
        await fill('12.34')
        await page.locator('.refund-confirm').click()
        await page.waitForFunction(() => document.querySelector('.refund-confirm')?.hasAttribute('disabled'))
        for (const selector of ['.refund-confirm', '.refund-cancel', '.refund-input-wrap input']) assert.notEqual(await page.locator(selector).getAttribute('disabled'), null)
        await background()
        assert.equal(await modal.count(), 1, '提交中不能点击背景关闭')
        assert.ok(releaseFirst)
        releaseFirst()
        await page.locator('.refund-error').filter({ hasText: '合成退款失败' }).waitFor()
        assert.equal(await input.inputValue(), '12.34', '失败后金额草稿保留')
        await page.locator('.refund-confirm').click()
        await modal.waitFor({ state: 'detached' })
        await page.locator('.refund-record-item').waitFor()
        assert.equal(writes.length, 2)
        assert.equal(writes[0].amountCents, 1234)
        assert.equal(writes[1].amountCents, 1234)
        assert.equal(writes[0].idempotencyKey, writes[1].idempotencyKey)
        assert.equal(await page.evaluate(() => document.documentElement.scrollWidth), width)
        console.log(JSON.stringify({ viewport: [width, height], type, inputAndClose: 'PASS', amountAndRetry: 'PASS', savingGuard: 'PASS', syntheticWrites: writes.length }))
        await page.close()
      }
    }
  } finally { await browser.close() }
})().catch(error => { console.error(error); process.exitCode = 1 })
