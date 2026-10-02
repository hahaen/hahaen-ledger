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
const variants = [
  { variant: 'brand' }, { variant: 'welcome', pageTopExtra: 60 },
  { variant: 'screen', compact: true }, { variant: 'screen', compact: true, fullWidth: false },
  { variant: 'help', compact: true }, { variant: 'help', pageTopExtra: 20 },
];
(async () => {
  const browser = await chromium.launch({ headless: true, executablePath: process.env.CHROME_EXECUTABLE_PATH });
  try {
    for (const [width, height] of [[320,568], [375,667], [430,932], [667,375]]) {
      for (const props of variants) {
        const page = await browser.newPage({ viewport: { width, height } });
        const menu = { top: 60, height: 32, rootInset: 54, rightPadding: 100 };
        const bottom = props.variant === 'brand' ? 20 : props.variant === 'welcome' ? 8 : props.variant === 'help' ? (props.compact ? 10 : 16) : (props.compact ? 10 : 20);
        await page.setContent(`<style>body{margin:0}view{display:block;box-sizing:border-box}button{margin:0;border:0}#app{padding:${54 + (props.pageTopExtra || 0)}px 16px 0}.content{height:1800px;background:#dff2ef}${descriptor.styles[0].content}</style><div id="app"></div><script>${vue}</script>`);
        await page.evaluate(({code, props, menu}) => {
          const exports = {};
          new Function('require', 'exports', code)(name => name === 'vue' ? Vue : name.includes('nativeNavigation') ? { getNativeMenuMetrics: () => menu } : { staticResource: () => '' }, exports);
          window.backCount = 0;
          Vue.createApp({ render: () => [Vue.h(exports.default, {...props, title: '测试标题', onBack: () => window.backCount++}), Vue.h('div', {class: 'content'}, '正文')] }).mount('#app');
        }, { code, props, menu });
        const nav = page.locator('.native-navigation-fixed');
        const first = await nav.boundingBox();
        const content = await page.locator('.content').boundingBox();
        assert.equal(content.y, menu.top + menu.height + bottom, '首段内容应保持原位置');
        for (const y of [300, 600, 0]) {
          await page.evaluate(y => window.scrollTo(0,y), y);
          assert.equal(await page.evaluate(() => window.scrollY), y);
          assert.deepEqual(await nav.boundingBox(), first, '正文滚动时顶栏不得移动');
        }
        if (props.variant === 'screen' || props.variant === 'help') {
          await page.locator('button').click();
          assert.equal(await page.evaluate(() => window.backCount), 1);
        }
        assert.equal(await page.evaluate(() => document.documentElement.scrollWidth), width, '不得产生横向溢出');
        console.log(JSON.stringify({viewport:[width,height],props,contentY:content.y,fixedY:first.y,result:'PASS'}));
        await page.close();
      }
    }
  } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
