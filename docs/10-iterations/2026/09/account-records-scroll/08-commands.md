# commands

| 命令 | 结果 |
| --- | --- |
| `node --test tests/account-layout.test.mjs tests/calendar-layout.test.mjs tests/page-flows.test.mjs tests/entry.test.mjs` | 20/20 PASS |
| `pnpm run typecheck` | exit 0，PASS |
| `$accountBuildApiBaseUrl = 'http://127.0.0.1:8080'; $env:VITE_API_BASE_URL = $accountBuildApiBaseUrl; pnpm run build:h5` | `DONE Build complete.`，PASS |

构建日志仅包含本地占位 API 地址，无真实凭证。
