# 实际执行命令

在 `app/` 目录实际执行：

```powershell
pnpm exec node --test tests/page-flows.test.mjs
pnpm run typecheck
$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5
pnpm run build:mp-weixin
```

结果：前端流程回归 28/28 通过；`vue-tsc --noEmit` 通过；H5 与微信小程序生产构建均完成。两次构建仅出现既有 Dart Sass legacy JS API 弃用警告及 uni-app 更新提示，未出现本次变更错误。
