# 命令

| 命令 | 结果 |
| --- | --- |
| `git diff --check` | PASS，exit 0 |
| `pnpm run typecheck`（`app`） | PASS，exit 0 |
| `VITE_API_BASE_URL=http://127.0.0.1:8080 pnpm run build:h5`（`app`） | PASS，DONE/exit 0 |
| `mvn -q -DskipTests compile`（`server`） | PASS，exit 0 |
| `mvn -q test`（`server`） | PASS，exit 0 |
| `mvn dependency:tree '-Dincludes=cn.dev33'`（`server`） | PASS，包含 `sa-token-redis-jackson` 和 `sa-token-redis-template` |
| `pnpm run dev:h5 -- --host 127.0.0.1`（`app`） | PASS，服务启动于 `http://127.0.0.1:5173/` |
| 本地 H5 登录页浏览器检查 | PARTIAL，页面可打开；因本机未启动后端，验证码请求显示网络异常 |

未记录任何密码、Token、Redis 密钥、MinIO 密钥或永久 URL。
