# 实际执行命令

已实际执行：

```powershell
# server/
mvn test "-Dtest=H5AuthServiceTest"
mvn test

# app/
pnpm exec node --test tests/page-flows.test.mjs
pnpm run typecheck
$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5
```

结果：认证定向测试 4/4、后端全量测试 43/43、前端流程回归 30/30 均通过；TypeScript 类型检查与 H5 生产构建均以退出码 0 完成。
