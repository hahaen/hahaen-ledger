// H5页面夹具验证：模拟API用于验证前端行为，不连接真实账户。
const assert = require('node:assert/strict')
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright')
const path = require('node:path')
;(async () => {
  const browser = await chromium.launch({ headless: true, executablePath: process.env.CHROME_EXECUTABLE_PATH })
  try {
    for (const [width, height] of [[320,568],[375,667],[414,896],[667,375]]) {
      for (const kind of ['FUND','CREDIT']) {
        const page = await browser.newPage({ viewport:{width,height}, isMobile:true, hasTouch:true })
        let account = {id:'1',name:'测试账户',kind,balanceCents:12345678,creditLimitCents:23456789,includedInNetAsset:true,status:'ACTIVE',sortOrder:1}
        const rows = Array.from({length:123},(_,i)=>({id:String(i+1),type:i%2?'INCOME':'EXPENSE',accountId:'1',amountCents:100,occurredAt:`${i<50?'2026-10-04':i<100?'2025-03-03':'2024-01-01'}T12:00:00`,note:`流水${i+1}`,status:'ACTIVE',hasRefund:false}))
        let saved, failNext=false
        const pages=[]
        await page.route('**/api/app/accounts/1', async route => {
          if (route.request().method()==='PUT') { saved=route.request().postDataJSON(); account={...account,...saved,balanceCents:kind==='FUND'?saved.balanceCents:saved.currentDebtCents} }
          await route.fulfill({json:{code:0,data:account}})
        })
        await page.route('**/api/app/accounts/1/transactions**', async route=>{
          const query=new URL(route.request().url()).searchParams
          const num=Number(query.get('page')||1),size=Number(query.get('pageSize')||50)
          pages.push(num)
          if(failNext) {failNext=false;return route.fulfill({status:503,json:{code:503,message:'夹具模拟加载失败'}})}
          const selected=rows.filter(row=>!query.get('type')||row.type===query.get('type'))
          await route.fulfill({json:{code:0,data:{items:selected.slice((num-1)*size,num*size),total:selected.length,page:num,pageSize:size}}})
        })
        await page.route('**/api/app/accounts',route=>route.fulfill({json:{code:0,data:[account]}}))
        await page.goto('http://127.0.0.1:18761/#/pages/account/account?id=1')
        await page.getByText('123 笔记录',{exact:true}).waitFor()
        assert.equal(await page.locator('.transaction-item').count(),50)
        await page.getByText('编辑账户',{exact:true}).click()
        const fields=page.locator('.asset-edit-modal input')
        assert.equal(await fields.nth(1).inputValue(),kind==='FUND'?'123456.78':'234567.89')
        if(kind==='CREDIT') assert.equal(await fields.nth(2).inputValue(),'123456.78')
        await fields.nth(0).fill('已编辑账户')
        await fields.nth(1).fill(kind==='FUND'?'-1000.29':'300000.12')
        if(kind==='CREDIT') await fields.nth(2).fill('-2000.01')
        await page.locator('.asset-create-save').click()
        await page.waitForFunction(()=>!document.querySelector('.asset-edit-modal'))
        assert.equal(saved.name,'已编辑账户')
        assert.equal(kind==='FUND'?saved.balanceCents:saved.currentDebtCents,kind==='FUND'?-100029:-200001)
        if(kind==='CREDIT')assert.equal(saved.creditLimitCents,30000012)
        await page.getByText('123 笔记录',{exact:true}).waitFor()
        const scroll=page.locator('.account-record-list .uni-scroll-view-scrollbar-hidden').first()
        const bottom=async()=>{await page.waitForTimeout(250);await scroll.evaluate(async el=>{await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));el.scrollTop=el.scrollHeight});}
        failNext=true
        await bottom()
        await page.getByText('加载失败，点击重试',{exact:true}).waitFor()
        await page.getByText('加载失败，点击重试',{exact:true}).click()
        await page.waitForFunction(()=>document.querySelectorAll('.transaction-item').length===100)
        await bottom()
        await page.getByText('已显示全部 123 笔流水',{exact:true}).waitFor()
        assert.equal(await page.locator('.transaction-item').count(),123)
        assert.ok(pages.includes(2)&&pages.includes(3))
        await page.locator('.filter-row').getByText('支出',{exact:true}).click()
        await page.getByText('62 笔记录',{exact:true}).waitFor()
        await bottom()
        await page.getByText('已显示全部 62 笔流水',{exact:true}).waitFor()
        assert.equal(await page.locator('.transaction-item').count(),62)
        assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth),width)
        if(width===375) { await page.waitForFunction(()=>!document.body.innerText.includes('夹具模拟加载失败')); await scroll.evaluate(el=>{el.scrollTop=0}); await page.screenshot({path:path.resolve(__dirname,`../../docs/10-iterations/2026/10/item-account-detail-fixes/evidence/account-${kind.toLowerCase()}.png`)}) }
        console.log(JSON.stringify({viewport:[width,height],kind,edit:true,all123Records:true,filter62:true,retry:true,result:'PASS'}))
        await page.close()
      }
      const page=await browser.newPage({viewport:{width,height},isMobile:true,hasTouch:true})
      await page.goto('http://127.0.0.1:18761/#/pages/item-detail/item-detail?id=103')
      await page.locator('.item-cost-segment').first().waitFor()
      assert.equal(await page.locator('.item-chart-card canvas').count(),0)
      await page.evaluate(()=>window.scrollTo(0,document.documentElement.scrollHeight))
      const note=await page.locator('.item-independence').boundingBox(),bar=await page.locator('.item-detail-actions').boundingBox()
      assert.ok(note.y+note.height<=bar.y,JSON.stringify({note,bar}))
      assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth),width)
      await page.getByText('重新服役',{exact:true}).click()
      await page.getByText('确认服役',{exact:true}).waitFor()
      await page.getByText('取消',{exact:true}).click()
      await page.getByText('编辑',{exact:true}).click()
      await page.locator('.modal-backdrop').waitFor()
      await page.getByText('取消',{exact:true}).click()
      if(width===375)await page.screenshot({path:path.resolve(__dirname,'../../docs/10-iterations/2026/10/item-account-detail-fixes/evidence/retired-item.png')})
      console.log(JSON.stringify({viewport:[width,height],retiredChart:true,bottomClearance:true,editAndReactivate:true,result:'PASS'}))
      await page.close()
    }
  } finally { await browser.close() }
})().catch(error=>{console.error(error);process.exitCode=1})
