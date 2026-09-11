# 命令

在 `app/` 目录实际执行：

```powershell
pnpm run typecheck
$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5
node --test tests/page-flows.test.mjs
```

三条命令均 exit 0；Node 测试为 17/17 通过。构建仅输出既有 Dart Sass legacy-js-api 弃用警告；未输出编译错误。未记录任何密钥、令牌或账号信息。
