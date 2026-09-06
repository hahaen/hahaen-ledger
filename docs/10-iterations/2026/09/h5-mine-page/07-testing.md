# 测试

## 已执行

| 范围 | 结果 | 证据 |
| --- | --- | --- |
| 前端类型检查 | PASS | `pnpm run typecheck`，exit 0 |
| H5 生产构建 | PASS | `VITE_API_BASE_URL=http://127.0.0.1:8080 pnpm run build:h5`，DONE/exit 0 |
| 后端测试 | PASS | `mvn test`，4 tests，0 failures/errors/skipped |
| H5 真实登录态页面 | NOT_RUN | 当前未启动后端且没有可用联调账号/头像文件，未伪造会话 |
| MinIO 头像上传 | BLOCKED | 需要可用 MinIO、CORS 和已登录会话 |
| 微信开发者工具 | NOT_RUN | 不在本轮范围 |
