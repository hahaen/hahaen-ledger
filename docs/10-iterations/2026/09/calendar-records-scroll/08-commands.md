# commands

| 命令 | 结果 |
| --- | --- |
| `node --test tests/calendar-layout.test.mjs tests/page-flows.test.mjs tests/entry.test.mjs` | 19/19 PASS |
| `pnpm run typecheck` | exit 0，PASS |
| `$calendarBuildApiBaseUrl = 'http://127.0.0.1:8080'; $env:VITE_API_BASE_URL = $calendarBuildApiBaseUrl; pnpm run build:h5` | `DONE Build complete.`，PASS |

构建日志仅包含本地占位 API 地址，无真实凭证。
