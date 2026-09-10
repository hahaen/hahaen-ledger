# testing

- `node --test tests/account-layout.test.mjs tests/calendar-layout.test.mjs tests/page-flows.test.mjs tests/entry.test.mjs`：20/20 PASS。
- `pnpm run typecheck`：exit 0，PASS。
- `$accountBuildApiBaseUrl = 'http://127.0.0.1:8080'; $env:VITE_API_BASE_URL = $accountBuildApiBaseUrl; pnpm run build:h5`：输出 `DONE Build complete.`，PASS。
- 独立只读视觉夹具资金账户详情：页面 `scrollTop=0`，账户流水滚动节点 `scrollTop=47.33`，筛选按钮和账户卡片位置不变，PASS。
- 日期分组吸顶核对：日期标题计算样式为 `position:sticky; top:0; z-index:2`，流水滚动至 `scrollTop=47.33` 时页面仍为 `scrollTop=0`，PASS。
