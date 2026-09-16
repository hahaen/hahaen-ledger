# commands

以下命令均在本轮实际执行，未包含凭证。

```powershell
cd app
pnpm exec node --test tests/*.test.mjs
pnpm run typecheck
$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5
$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin
git diff --check
```

结果：Node 回归 55/55 PASS；类型检查、H5 生产构建、微信小程序生产构建和空白检查均 PASS。
