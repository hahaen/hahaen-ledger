// 固定启动页文案图，不读取账号、账单、余额或当前页面。
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright');
const { resolve } = require('node:path');
const { pathToFileURL } = require('node:url');
const assert = require('node:assert/strict');
(async () => {
  const browser = await chromium.launch({ headless: true, executablePath: process.env.CHROME_EXECUTABLE_PATH });
  try {
    const page = await browser.newPage({ viewport: { width: 500, height: 400 }, deviceScaleFactor: 1 });
    await page.goto(pathToFileURL(resolve(__dirname, '../assets-source/wechat-welcome.html')).href);
    await page.evaluate(() => document.fonts.ready);
    assert.equal(await page.locator('img').count(), 0);
    const text = await page.locator('body').innerText();
    assert.ok(text.includes('记录每一笔') && text.includes('让生活更清晰'));
    assert.doesNotMatch(text, /账单|余额|本月支出|本月收入|最近记账/);
    assert.equal(await page.evaluate(() => document.body.scrollHeight <= 400 && document.body.scrollWidth <= 500), true);
    await page.screenshot({ path: resolve(__dirname, '../offloaded-static-assets/wx/share-welcome.png') });
    console.log('PASS：500×400启动页欢迎图，仅固定文案，无账单余额，无溢出');
  } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
