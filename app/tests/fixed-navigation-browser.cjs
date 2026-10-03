// 实际 NativeNavigation SFC 的只读布局回归；微信胶囊几何为夹具，不替代真机验收。
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const assert = require('node:assert/strict');
const { parse, compileScript } = require('vue/compiler-sfc');
const ts = require('typescript');
const { chromium } = require(process.env.PLAYWRIGHT_MODULE_PATH || 'playwright');
const source = readFileSync(resolve(__dirname, '../src/components/NativeNavigation.vue'), 'utf8');
const { descriptor } = parse(source);
const script = compileScript(descriptor, { id: 'navigation-test', inlineTemplate: true });
const code = ts.transpileModule(script.content, { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ESNext } }).outputText;
const vue = readFileSync(require.resolve('vue/dist/vue.global.prod.js'), 'utf8');
// 从真实调用方读取组件参数；页面padding独立给定，避免漏传补偿却仍通过。
const variants = [
  ['components/PageHeader.vue', 20], ['pages/first-use/first-use.vue', 60],
  ['pages/entry/entry.vue', 0], ['pages/detail/detail.vue', 0],
  ['pages/help/help.vue', 0], ['pages/profile/profile.vue', 20],
  ['pages/item-detail/item-detail.vue', 20], ['pages/account/account.vue', 20],
  ['pages/notification-center/notification-center.vue', 20], ['pages/ha-todo/ha-todo.vue', 20],
  ['pages/ha-todo-detail/ha-todo-detail.vue', 20], ['pages/ha-todo-editor/ha-todo-editor.vue', 20],
  ['components/LegalDocumentPage.vue', 0],
].map(([file, pagePadding]) => {
  const source = readFileSync(resolve(__dirname, '../src', file), 'utf8');
  const call = source.match(/<NativeNavigation\b[^>]+>/)[0];
  return { file, pagePadding, props: {
    variant: call.match(/variant="([^"]+)"/)[1],
    compact: /\bcompact\b/.test(call),
    fullWidth: !/:full-width="false"/.test(call),
    pageTopExtra: Number(call.match(/:page-top-extra="(\d+)"/)?.[1] ?? 0),
  }};
});
(async () => {
  const browser = await chromium.launch({ headless: true, executablePath: process.env.CHROME_EXECUTABLE_PATH });
  try {
    for (const [width, height] of [[320,568], [375,667], [430,932], [667,375]]) {
      for (const actualRootInset of [25, 54]) {
      for (const {file, pagePadding, props} of variants) {
        const page = await browser.newPage({ viewport: { width, height } });
        const menu = { top: 60, height: 32, rootInset: 54, rightPadding: 100 };
        const bottom = 10;
        await page.setContent(`<style>body{margin:0}view{display:block;box-sizing:border-box}button{margin:0;border:0}#app{--status-bar-height:${actualRootInset}px;padding:calc(max(var(--status-bar-height, 25px), env(safe-area-inset-top, 0px)) + ${pagePadding}px) 16px 0}.content{height:1800px;background:#dff2ef}${descriptor.styles[0].content}</style><div id="app"></div><script>${vue}</script>`);
        await page.evaluate(({code, props, menu}) => {
          const exports = {};
          new Function('require', 'exports', code)(name => name === 'vue' ? Vue : name.includes('nativeNavigation') ? { getNativeMenuMetrics: () => menu } : { staticResource: () => '' }, exports);
          window.backCount = 0;
          window.uni = {
            createSelectorQuery() {
              let selector, callback;
              return { in() { return this; }, select(value) { selector = value; return this; },
                boundingClientRect(value) { callback = value; return this; },
                exec() { callback(document.querySelector(selector)?.getBoundingClientRect()); },
              };
            },
          };
          Vue.createApp({ render: () => [Vue.h(exports.default, {...props, title: '测试标题', onBack: () => window.backCount++}), Vue.h('div', {class: 'content'}, '正文')] }).mount('#app');
        }, { code, props, menu });
        await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
        const nav = page.locator('.native-navigation-fixed');
        const first = await nav.boundingBox();
        const content = await page.locator('.content').boundingBox();
        assert.equal(content.y, menu.top + menu.height + bottom, '首段内容应保持约定导航间距');
        // 微信切页后状态栏CSS变量可能晚于mounted测量更新，不能把首屏快照固化成补偿。
        const updatedRootInset = actualRootInset === 25 ? 54 : 25;
        await page.locator('#app').evaluate((el, inset) => el.style.setProperty('--status-bar-height', `${inset}px`), updatedRootInset);
        assert.equal((await page.locator('.content').boundingBox()).y, menu.top + menu.height + bottom,
          '安全区更新后正文间距仍应为10px，不得出现大空隙');
        for (const y of [300, 600, 0]) {
          await page.evaluate(y => window.scrollTo(0,y), y);
          assert.equal(await page.evaluate(() => window.scrollY), y);
          assert.deepEqual(await nav.boundingBox(), first, '正文滚动时顶栏不得移动');
        }
        if (props.variant === 'screen' || props.variant === 'help') {
          await page.locator('button').click();
          assert.equal(await page.evaluate(() => window.backCount), 1);
        }
        assert.equal((await page.locator('.content').boundingBox()).y, menu.top + menu.height + bottom,
          '往返滚动后首段间距不得增大');
        assert.equal(await page.evaluate(() => document.documentElement.scrollWidth), width, '不得产生横向溢出');
        console.log(JSON.stringify({viewport:[width,height],actualRootInset,file,props,contentY:content.y,updatedRootInset,fixedY:first.y,result:'PASS'}));
        await page.close();
      }
    }
    }
  } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
