# testing

- `node --test tests/calendar-layout.test.mjs tests/page-flows.test.mjs tests/entry.test.mjs`：19/19 PASS。
- `pnpm run typecheck`：exit 0，PASS。
- `$calendarBuildApiBaseUrl = 'http://127.0.0.1:8080'; $env:VITE_API_BASE_URL = $calendarBuildApiBaseUrl; pnpm run build:h5`：输出 `DONE Build complete.`，PASS。
- 独立只读视觉夹具 `http://127.0.0.1:18761/#/pages/calendar/calendar`：记录区域滚动后页面 `scrollTop=0`，记录滚动节点 `scrollTop=159.33`，PASS。
