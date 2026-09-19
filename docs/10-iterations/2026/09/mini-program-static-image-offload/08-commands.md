# 实际命令

- `pnpm run typecheck`：通过。
- `pnpm exec node --test tests/*.test.mjs`：55/55 通过。
- `pnpm run build:mp-weixin`：通过。
- PowerShell `Invoke-WebRequest -Method Head`：对 8 个公开对象返回 HTTP 200，且 Content-Length 与本地原图一致。
- 构建后递归统计 `app/dist/build/mp-weixin`：276678 bytes（270.19 KB）。
