// H5生产页面只读夹具验证：触摸滚动、固定边界、末笔可达及日期/月份交互。
const assert = require('node:assert/strict');
const { mkdirSync } = require('node:fs');
const { resolve } = require('node:path');
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright');
(async () => {
  const browser = await chromium.launch({ headless: true, executablePath: process.env.CHROME_EXECUTABLE_PATH });
  try {
    for (const [width, height] of [[320,568],[375,667],[430,932],[667,375]]) {
      const ctx = await browser.newContext({ viewport: { width, height }, isMobile: true, hasTouch: true });
      const page = await ctx.newPage();
      let empty = false, failure = false;
      await page.route('**/api/app/calendar/*', async route => {
        if (failure) return route.fulfill({ status: 503, json: { code: 503, message: '只读夹具失败' } });
        const date = new URL(route.request().url()).pathname.split('/').pop();
        const transactions = empty ? [] : Array.from({ length: 12 }, (_, i) => ({ id: String(100+i), type: 'EXPENSE', accountId: '2', amountCents: 100+i, occurredAt: `${date}T12:20:00`, hasRefund: false, status: 'ACTIVE' }));
        await route.fulfill({ json: { code: 0, data: { date, expenseCents: 1266, incomeCents: 0, balanceCents: -1266, transactions } } });
      });
      await page.goto('http://127.0.0.1:18761/#/pages/calendar/calendar');
      await page.locator('.transaction-item').last().waitFor();
      const header = page.locator('.page-header'), nav = page.locator('.bottom-nav');
      const initialHeader = await header.boundingBox(), initialNav = await nav.boundingBox();
      const scroller = page.locator('.calendar-content .uni-scroll-view').last();
      const calendar = await page.locator('.calendar-card').boundingBox();
      const cdp = await ctx.newCDPSession(page);
      async function swipe(up) {
        const box = await page.locator('.calendar-content').boundingBox();
        const high = box.y + Math.min(30,box.height/4), low = box.y + box.height - 25;
        const start = up ? low : high, end = up ? high : low;
        await cdp.send('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x:width/2,y:start}]});
        for(let i=1;i<=10;i++) {
          await cdp.send('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x:width/2,y:start+(end-start)*i/10}]});
          await page.waitForTimeout(20);
        }
        await cdp.send('Input.dispatchTouchEvent',{type:'touchEnd',touchPoints:[]});
        await page.waitForTimeout(150);
      }
      await swipe(true);
      assert.ok(await scroller.evaluate(el=>el.scrollTop)>20,'主体必须响应触摸上滑');
      assert.ok((await page.locator('.calendar-card').boundingBox()).y < calendar.y-20,'月历随账单移动');
      assert.deepEqual(await header.boundingBox(),initialHeader);
      assert.deepEqual(await nav.boundingBox(),initialNav);
      for(let i=0;i<10;i++) await swipe(true);
      const last = await page.locator('.transaction-item').last().boundingBox();
      assert.ok(last.y+last.height <= initialNav.y+1,'最后一笔不得被底栏遮挡');
      assert.ok(last.y>=initialHeader.y+initialHeader.height,'最后一笔可见');
      assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth),width);
      assert.equal(await page.evaluate(()=>window.scrollY),0);
      if(width===375) {
        const dir=resolve(__dirname,'../../docs/10-iterations/2026/10/calendar-content-scroll/evidence');
        mkdirSync(dir,{recursive:true});
        await page.screenshot({path:resolve(dir,'h5-scrolled-375x667.png')});
      }
      await scroller.evaluate(el=>{el.scrollTop=0});
      await page.waitForTimeout(200);
      await page.locator('[aria-label="下个月"]').click();
      await page.waitForTimeout(300);
      const gridCount=await page.locator('.calendar-cell').count();
      assert.equal(gridCount,35);
      await page.locator('[aria-label="下个月"]').click();
      await page.waitForTimeout(300);
      await page.locator('[aria-label="下个月"]').click();
      await page.waitForTimeout(300);
      assert.equal(await page.locator('.calendar-cell').count(),42);
      empty=true;
      await page.locator('.calendar-cell:not(.muted)').nth(5).click();
      await page.getByText('这一天还没有记账记录').waitFor();
      failure=true;
      await page.locator('.calendar-cell:not(.muted)').nth(6).click();
      await page.getByText('暂时无法加载当天账单').waitFor();
      failure=false; empty=false;
      await page.getByText('重试',{exact:true}).click();
      await page.locator('.transaction-item').last().waitFor();
      await page.locator('.calendar-month').click();
      await page.locator('.month-picker-backdrop').waitFor();
      console.log(JSON.stringify({viewport:[width,height],rows:12,lastBottom:last.y+last.height,navY:initialNav.y,months:'35/42格',emptyRetry:'PASS',result:'PASS'}));
      await ctx.close();
    }
  } finally { await browser.close(); }
})().catch(error=>{console.error(error);process.exitCode=1});
