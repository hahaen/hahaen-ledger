// H5生产页面只读夹具：月度/单日口径、切月、失败重试及窄屏长金额。
const assert = require('node:assert/strict');
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright');
(async()=>{
const browser=await chromium.launch({headless:true,executablePath:process.env.CHROME_EXECUTABLE_PATH});
try {
for(const [width,height] of [[320,568],[375,667],[414,896],[667,375]]) {
const page=await browser.newPage({viewport:{width,height},isMobile:true,hasTouch:true});
let fail=false,large=false;
await page.route('**/api/app/home/summary?*',async route=>{
const month=new URL(route.request().url()).searchParams.get('month');
const expenseCents=Number(month.slice(-2))*10000;
await route.fulfill({json:{code:0,data:{month,expenseCents,incomeCents:200000,balanceCents:200000-expenseCents,dailyExpenseCents:100,transactions:[]}}});
});
await page.route('**/api/app/calendar/*',async route=>{
if(fail)return route.fulfill({status:503,json:{code:503,message:'只读夹具失败'}});
const date=new URL(route.request().url()).pathname.split('/').pop();const day=Number(date.slice(-2));
const expenseCents=large?999999999999:day===2?0:1200;
const incomeCents=large?888888888888:day===2?0:3400;
const transactions=day===2?[]:[{id:'1',type:'EXPENSE',amountCents:expenseCents,occurredAt:date+'T12:00:00',status:'ACTIVE',hasRefund:false}];
await route.fulfill({json:{code:0,data:{date,expenseCents,incomeCents,balanceCents:incomeCents-expenseCents,transactions}}});
});
await page.goto('http://127.0.0.1:18761/#/pages/calendar/calendar');
await page.locator('.calendar-date-meta .date-flow').waitFor();
const totals=await page.locator('.day-summary').innerText();
assert.match(totals,/当月支出/);assert.match(totals,/当月收入/);assert.match(totals,/当月结余/);
await page.locator('.calendar-cell:not(.muted)').nth(0).click();
await page.waitForFunction(()=>document.querySelector('.date-title')?.textContent.endsWith('月1日')&&document.querySelector('.calendar-date-meta .date-flow'));
assert.equal(await page.locator('.day-summary').innerText(),totals);
assert.match(await page.locator('.calendar-date-meta').innerText(),/收\s*34[\s\S]*支\s*12[\s\S]*1\s*笔/);
const flow=await page.locator('.calendar-date-meta .date-flow').boundingBox();const count=await page.locator('.calendar-date-meta .section-meta').boundingBox();
assert.ok(flow.x+flow.width<=count.x+1);
await page.locator('.calendar-cell:not(.muted)').nth(1).click();
await page.getByText('这一天还没有记账记录').waitFor();
assert.match(await page.locator('.calendar-date-meta').innerText(),/收\s*0[\s\S]*支\s*0[\s\S]*0\s*笔/);
fail=true;await page.locator('.calendar-cell:not(.muted)').nth(2).click();
await page.getByText('暂时无法加载当天账单').waitFor();
assert.equal(await page.locator('.calendar-date-meta .date-flow').count(),0);
assert.equal(await page.locator('.day-summary').innerText(),totals);
fail=false;await page.getByText('重试',{exact:true}).click();await page.locator('.calendar-date-meta .date-flow').waitFor();
await page.locator('[aria-label="下个月"]').click();
await page.waitForFunction(old=>document.querySelector('.day-summary')?.innerText!==old&&document.querySelector('.calendar-date-meta .date-flow'),totals);
assert.match(await page.locator('.day-summary').innerText(),/当月支出/);
await page.waitForFunction(()=>!document.querySelector('.uni-toast'));// 等待失败提示关闭再保存证据
if(width===375)await page.screenshot({path:require('node:path').resolve(__dirname,'../../docs/10-iterations/2026/10/calendar-month-summary/evidence/h5-375.png')});
large=true;await page.locator('.calendar-cell:not(.muted)').nth(3).click();
await page.waitForFunction(()=>document.querySelector('.calendar-date-meta .date-flow')?.textContent.includes('9,999,999,999.99'));
assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth),width);
console.log(JSON.stringify({viewport:[width,height],monthlyStable:true,dailyAndEmpty:true,failureRetry:true,monthChange:true,largeAmountOverflow:false,result:'PASS'}));
await page.close();
}
} finally {await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
