# 测试

## 计划

- 前端 `pnpm run typecheck`。
- H5 生产构建，注入本地 API 地址。
- 后端 `mvn test` 和编译，包含 Redis DAO Bean 单元测试。
- 启动 Redis 后检查 Sa-Token DAO Bean 和登录会话跨服务重启恢复。
- 浏览器验证多个 H5 路由刷新后 URL 与页面保持不变。

真实基础设施或浏览器未启动时，相关项必须记录为 BLOCKED 或 NOT_RUN，不以静态代码代替运行证据。
