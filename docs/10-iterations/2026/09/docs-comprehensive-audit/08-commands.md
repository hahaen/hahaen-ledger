# 审计命令与结果

以下只记录本次真实执行的命令摘要，不包含凭证或完整环境变量。

| 命令/检查 | 结果 |
| --- | --- |
| `Get-ChildItem docs -Recurse -File`、逐文件 `Get-Content` | PASS：完成 docs 全量清点和阅读 |
| `git status --short` | PASS：确认工作区已有用户代码修改；未覆盖或撤销 |
| `mvn test`（`server`） | PASS：18 tests，0 failures/errors/skipped |
| `pnpm run typecheck`（`app`） | PASS |
| `VITE_API_BASE_URL=http://127.0.0.1:8080 pnpm run build:h5` | PASS |
| `VITE_API_BASE_URL=http://127.0.0.1:8080 pnpm run build:mp-weixin` | PASS |
| `GET http://127.0.0.1:8080/api-docs` | PASS：HTTP 200 |
| `GET http://127.0.0.1:8080/api/app/accounts` | PASS：未登录 HTTP 401 |
| `GET http://127.0.0.1:8080/api/app/auth/captcha` | PASS：HTTP 200 |
| `Get-NetTCPConnection -State Listen`（3306/6379/9000） | 未发现监听；MySQL/Redis/MinIO 运行验证 BLOCKED |
| Markdown 相对链接检查 | PASS：未发现失效目标 |

