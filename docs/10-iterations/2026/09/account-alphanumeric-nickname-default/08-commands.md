# 实际执行命令

实际执行：

```powershell
# app/
pnpm exec node --test tests/page-flows.test.mjs
pnpm run typecheck
$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5
pnpm run build:mp-weixin

# server/
mvn test "-Dtest=H5AuthServiceTest,ProfileServiceTest"
mvn test
```

结果：前端流程回归 29/29 通过；定向后端测试 12/12 通过；后端全量测试 40/40 通过；类型检查及 H5、微信小程序生产构建均通过。构建仅输出既有 Sass legacy JS API 弃用警告和 uni-app 更新提示。
