// 生产 H5 只读夹具触摸回归；先启动 visual-server.mjs。禁止用于真实记账写入。
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright');
const assert = require('node:assert/strict');
(async () => {
    const browser = await chromium.launch({ headless: true, executablePath: process.env.CHROME_EXECUTABLE_PATH });
    try {
    for (const [width, height] of [[375, 667], [320, 568], [390, 844], [667, 375]]) {
    for (const query of ['', '?id=2', '?id=1', '?id=6']) {
        const ctx = await browser.newContext({ viewport: { width, height }, isMobile: true, hasTouch: true });
        const page = await ctx.newPage();
        await page.goto('http://127.0.0.1:18761/#/pages/entry/entry' + query);
        await page.waitForTimeout(700);
        const scroll = page.locator('.entry-content .uni-scroll-view').last();
        const keypad = page.locator('.keypad');
        const before = await keypad.boundingBox();
        const cdp = await ctx.newCDPSession(page);
        async function swipe(up) { const end = up ? 100 : before.y - 30; const start = up ? before.y - 30 : 100; await cdp.send('Input.dispatchTouchEvent', { type: 'touchStart', touchPoints: [{ x: 170, y: start }] }); for (let i = 1; i <= 10; i++) {
            await cdp.send('Input.dispatchTouchEvent', { type: 'touchMove', touchPoints: [{ x: 170, y: start + (end - start) * i / 10 }] });
            await page.waitForTimeout(25);
        } await cdp.send('Input.dispatchTouchEvent', { type: 'touchEnd', touchPoints: [] }); await page.waitForTimeout(150); }
        await swipe(true);
        const top = await scroll.evaluate(el => el.scrollTop);
        const range = await scroll.evaluate(el => el.scrollHeight - el.clientHeight);
        if (range > 1)
            assert.ok(top > Math.min(20, range - 1), `not scrolling: ${width}x${height}${query}`);
        assert.deepEqual(await keypad.boundingBox(), before);
        for (let i = 0; i < 8; i++)
            await swipe(true);
        const note = page.locator('.fields-card .field-row').last();
        const box = await note.boundingBox();
        assert.ok(box.y + box.height <= before.y + 1, `note hidden: ${JSON.stringify(box)}`);
        const last = await scroll.evaluate(el => el.scrollTop);
        await swipe(false);
        if (last > 0)
            assert.ok(await scroll.evaluate(el => el.scrollTop) < last);
        await note.click();
        await page.locator('.entry-note-picker-cancel').click();
        assert.equal(await page.locator('.entry-note-picker-backdrop').count(), 0);
        console.log(JSON.stringify({ viewport: [width, height], query, scrollTop: top, scrollRange: range, noteBottom: box.y + box.height, keypadY: before.y, result: 'PASS' }));
        await ctx.close();
    }
}
    } finally { await browser.close(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
