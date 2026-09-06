# 命令

| 命令 | 结果 |
| --- | --- |
| `pnpm run typecheck`（`app`） | PASS，exit 0 |
| `pnpm run build:h5`（`app`，未配置 `VITE_API_BASE_URL`） | FAIL（预期保护），构建拒绝并提示缺少生产 API 地址 |
| `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`（`app`） | PASS，DONE/exit 0 |
| `mvn test`（`server`） | PASS，4 tests，0 failures/errors/skipped |
| 浏览器打开 `http://127.0.0.1:5173/#/pages/auth/login/login` | PASS，登录页 AX 文本可见；目标登录态页面未联调 |

未记录任何密码、Token、MinIO 密钥、AppSecret 或永久 URL。
