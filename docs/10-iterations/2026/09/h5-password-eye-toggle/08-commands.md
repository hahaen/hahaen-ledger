# 命令记录

在 `app` 目录实际执行：

- `pnpm run typecheck`：PASS，`vue-tsc --noEmit` 正常结束。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`：PASS，`DONE Build complete.`。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin`：PASS，`DONE Build complete.`。

构建过程仅输出 uni-app 更新提示和 Sass legacy API 弃用提示；未记录任何敏感配置值。
