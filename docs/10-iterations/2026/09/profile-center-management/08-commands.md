# 命令

- `cd server; mvn test`：PASS，33 tests，0 failures/errors/skipped。
- `cd app; node --test tests/page-flows.test.mjs tests/auth-guard.test.mjs`：PASS，25 tests passed。
- `cd app; pnpm run typecheck`：PASS，exit 0。
- `cd app; $env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`：PASS，完成；仅有既有 Sass legacy-js-api 弃用警告。
- `cd app; $env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin`：PASS，完成；仅有既有 Sass legacy-js-api 弃用警告。
- `cd app; node tests/visual-server.mjs` 后访问 `http://127.0.0.1:18761/#/pages/profile/profile`：PASS，只读夹具核对主页面和修改密码弹层；未提交写入。
- `cd server; mvn test`：PASS，34 tests，0 failures/errors/skipped。
- `cd app; pnpm run typecheck`：PASS，exit 0。
- `cd app; node --test tests/page-flows.test.mjs`：PASS，21 tests，0 failures。
