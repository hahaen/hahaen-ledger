# 命令

| 命令 | 结果 |
| --- | --- |
| `pnpm exec node --test tests/*.test.mjs`（`app/`） | PASS，54/54 |
| `pnpm run typecheck`（`app/`） | PASS，exit 0 |
| `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`（`app/`） | PASS，`DONE Build complete.` |
| `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin`（`app/`） | PASS，`DONE Build complete.` |
| `git diff --check` | PASS |

补充静态检查：微信 `app.wxss` 未发现主题色 `var(--...)` 引用；`home-page`、`calendar-page`、`assets-page`、`bottom-nav`、导航选中态及 PageHeader 均含明确颜色声明。真实小程序画面仍未执行。

本文件只记录实际执行结果，不记录任何凭证。
