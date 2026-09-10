# 命令

| 工作目录 | 命令 | 结果 |
| --- | --- | --- |
| `app` | `pnpm run typecheck` | PASS，`vue-tsc --noEmit` exit 0 |
| `app` | `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5` | PASS，`DONE Build complete.` |
| `app` | `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin` | PASS，`DONE Build complete.` |
| `app` | `node --test tests/entry.test.mjs tests/page-flows.test.mjs` | PASS，18/18；包含退款删除刷新失败和详情首次生命周期回归 |
| `server` | `mvn test` | PASS，26 tests，0 failures/errors/skipped；包含退款软删除与余额回滚回归 |
| `app` | `node tests/visual-server.mjs` + Codex 内置浏览器打开本机页面 | BLOCKED，浏览器返回 `ERR_BLOCKED_BY_CLIENT` |
