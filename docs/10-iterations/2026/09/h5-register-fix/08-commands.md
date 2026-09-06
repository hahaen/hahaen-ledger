# Commands

- `git diff --check`：PASS，无空白错误。
- `pnpm run typecheck`：PASS，`vue-tsc --noEmit` 通过。
- `pnpm run build:h5`：PASS；使用临时 `VITE_API_BASE_URL=http://127.0.0.1:8080` 环境变量构建通过。
- `mvn test`：PASS，3 个测试通过。
