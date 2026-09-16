# 命令

以下均为本次实际执行结果，未记录凭证或敏感配置：

| 命令 | 结果 |
| --- | --- |
| `cd app; node --test tests/*.test.mjs` | PASS，53/53 |
| `cd app; pnpm typecheck` | PASS，exit 0 |
| `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm build:h5` | PASS，`DONE Build complete.` |
| `cd app; pnpm build:mp-weixin` | PASS，`DONE Build complete.` |
| `git diff --check` | PASS，无差异错误 |
| `rg`/读取 `app/dist/build/mp-weixin/pages/calendar/calendar.wxml` 与 `app.wxss` | PASS，静态产物符合预期 |
