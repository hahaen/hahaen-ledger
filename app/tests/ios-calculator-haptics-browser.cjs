// 生产 H5 + 本地只读接口夹具；事件/布局回归不证明 iPhone 实际物理触感。
// 先启动 visual-server.mjs。写请求在本测试拦截，不发送到真实后端。
const { chromium, webkit } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright')
const assert = require('node:assert/strict')
const safari = 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 Version/18.0 Mobile/15E148 Safari/604.1'
const url = 'http://127.0.0.1:18761/haji/#/pages/entry/entry'
async function run(engine, name, launch = {}) {
  const browser = await engine.launch({ headless: true, ...launch })
  try {
    for (const [width, height] of [[320, 568], [390, 844], [844, 390]]) {
      const context = await browser.newContext({ viewport: { width, height }, userAgent: safari, isMobile: true, hasTouch: true })
      // Chromium 仅用能力夹具覆盖分支；WebKit 必须实际支持原生 switch，禁止伪造。
      if (name === 'chromium') await context.addInitScript(() => Object.defineProperty(HTMLInputElement.prototype, 'switch', { configurable: true, value: false }))
      const page = await context.newPage()
      const errors = []
      page.on('pageerror', error => errors.push(error.message))
      let finishWrite, writes = []
      await page.route('**/api/app/transactions', async route => {
        if (route.request().method() !== 'POST') return route.continue()
        writes.push(route.request().postDataJSON())
        await new Promise(resolve => { finishWrite = resolve })
        await route.fulfill({ status: 503, contentType: 'application/json', body: JSON.stringify({ code: 503, message: '夹具失败，请重试', data: null }) })
      })
      await page.goto(url)
      const overlays = page.locator('input.calculator-haptic-switch')
      await overlays.first().waitFor()
      assert.equal(await overlays.count(), 18)
      const key = label => page.locator('.keypad .key').filter({ hasText: new RegExp(`^${label}$`) })
      const style = await key('1').evaluate(el => {
        const css = getComputedStyle(el)
        return { appearance: css.appearance, border: css.borderTopWidth, background: css.backgroundColor, fontSize: css.fontSize }
      })
      assert.deepEqual(style, { appearance: 'none', border: '0px', background: 'rgba(0, 0, 0, 0)', fontSize: '29px' })
      assert.equal(await key('确定').evaluate(el => getComputedStyle(el).backgroundColor), 'rgb(240, 250, 247)')
      const tap = async label => {
        const button = key(label)
        const box = await button.boundingBox()
        const input = button.locator('..').locator('input.calculator-haptic-switch')
        const hit = await page.evaluate(({ x, y }) => document.elementFromPoint(x, y)?.className, { x: box.x + box.width * .8, y: box.y + box.height * .5 })
        assert.match(hit, /calculator-haptic-switch/)
        assert.equal(await input.getAttribute('aria-hidden'), 'true')
        assert.equal(await input.getAttribute('tabindex'), '-1')
        await page.touchscreen.tap(box.x + box.width * .8, box.y + box.height * .5)
      }
      await tap('1'); await tap('2'); await tap('\\.'); await tap('3')
      assert.equal(await page.locator('.calculation-line').textContent(), '12.3')
      await tap('⌫'); await tap('⌫'); await tap('\\+'); await tap('4')
      assert.equal(await page.locator('.calculation-line').textContent(), '12+4')
      assert.equal(await page.locator('.amount-value').textContent(), '16')
      await key('5').focus(); await page.keyboard.press('Enter')
      assert.equal(await page.locator('.calculation-line').textContent(), '12+45')
      await page.emulateMedia({ reducedMotion: 'reduce' })
      assert.equal(await overlays.first().evaluate(el => getComputedStyle(el).pointerEvents), 'none')
      await key('⌫').tap()
      assert.equal(await page.locator('.calculation-line').textContent(), '12+4')
      await page.emulateMedia({ reducedMotion: 'no-preference' })
      // 保存失败后仍可输入并以同一幂等键重试；每次点击只产生一次提交。
      await tap('确定')
      await page.waitForFunction(() => [...document.querySelectorAll('input.calculator-haptic-switch')].every(el => el.disabled))
      assert.equal(writes.length, 1)
      assert.equal(writes[0].amountCents, 1600)
      const disabled = await overlays.first().boundingBox()
      await page.touchscreen.tap(disabled.x + disabled.width / 2, disabled.y + disabled.height / 2)
      assert.equal(writes.length, 1)
      finishWrite()
      await page.waitForFunction(() => [...document.querySelectorAll('input.calculator-haptic-switch')].every(el => !el.disabled))
      await tap('再记')
      await page.waitForFunction(() => [...document.querySelectorAll('input.calculator-haptic-switch')].every(el => el.disabled))
      assert.equal(writes.length, 2)
      assert.equal(writes[0].idempotencyKey, writes[1].idempotencyKey)
      assert.equal(writes[1].amountCents, 1600)
      finishWrite()
      await page.waitForFunction(() => [...document.querySelectorAll('input.calculator-haptic-switch')].every(el => !el.disabled))
      assert.equal(await page.locator('.amount-value').textContent(), '16')
      assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth))
      assert.deepEqual(errors, [])
      console.log(`PASS ${name} ${width}x${height}: 原生覆盖命中、单次输入/保存、禁用、键盘、减少动态效果、失败幂等重试`)
      await context.close()
    }
    // 不支持 switch 时（包括旧 Safari）仍走原按钮，不影响正常输入。
    const fallback = await browser.newContext({ viewport: { width: 390, height: 844 }, hasTouch: true })
    const page = await fallback.newPage()
    await page.goto(url)
    await page.locator('.keypad .key').filter({ hasText: /^1$/ }).tap()
    assert.equal(await page.locator('.calculation-line').textContent(), '1')
    assert.equal(await page.locator('input.calculator-haptic-switch').count(), 0)
    console.log(`PASS ${name}: 非 iPhone Safari 普通按键回退`)
    await fallback.close()
  } finally { await browser.close() }
}
;(async () => {
  await run(chromium, 'chromium', { executablePath: process.env.CHROME_EXECUTABLE_PATH })
  await run(webkit, 'webkit', { executablePath: process.env.WEBKIT_EXECUTABLE_PATH })
})().catch(error => { console.error(error); process.exitCode = 1 })
