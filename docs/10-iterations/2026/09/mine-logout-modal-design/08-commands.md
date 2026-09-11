# 命令

- `cd app; node --test tests/page-flows.test.mjs`：PASS，19 tests passed。
- `cd app; pnpm run typecheck`：PASS，exit 0。
- `cd app; $env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`：PASS，输出 `DONE Build complete.`。
- `cd app; $env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin`：PASS，输出 `DONE Build complete.`。
- `cd app; node tests/visual-server.mjs`：启动 PASS；内置浏览器访问 `http://127.0.0.1:18761/pages/mine/mine`：BLOCKED，`ERR_BLOCKED_BY_CLIENT`。
