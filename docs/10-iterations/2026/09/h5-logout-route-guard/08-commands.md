# 命令

以下命令均在 `app/` 目录执行，未包含任何凭证：

- `pnpm exec vue-tsc --noEmit`：exit 0。
- `node --test tests/auth-guard.test.mjs tests/page-flows.test.mjs`：23/23 通过。
- `node --test tests/auth-guard.test.mjs tests/entry.test.mjs tests/page-flows.test.mjs tests/calendar-layout.test.mjs tests/account-layout.test.mjs`：28/28 通过。
- 临时设置 `VITE_API_BASE_URL=http://127.0.0.1:8080` 后执行 `pnpm run build:h5`：`DONE Build complete.`，exit 0；随后移除临时环境变量。
- 临时设置 `VITE_API_BASE_URL=http://127.0.0.1:8080` 后执行 `pnpm run build:mp-weixin`：`DONE Build complete.`，exit 0；随后移除临时环境变量。
- `git diff --check`：exit 0；仅显示既有 CRLF 转换提示，无 whitespace error。
