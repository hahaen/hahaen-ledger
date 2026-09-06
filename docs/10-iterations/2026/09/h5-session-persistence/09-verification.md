# 验证

| 项目 | 状态 | 证据 |
| --- | --- | --- |
| 前端类型检查 | PASS | `pnpm run typecheck` exit 0 |
| H5 生产构建 | PASS | 注入本地 API 地址后 `pnpm run build:h5` DONE/exit 0 |
| 后端测试/编译 | PASS | `mvn -q -DskipTests compile`、`mvn -q test` exit 0；含 Redis DAO Bean 测试 |
| Sa-Token Redis 依赖 | PASS | `mvn dependency:tree '-Dincludes=cn.dev33'` 包含 Redis Jackson/Template DAO |
| Sa-Token Bean 唯一性 | PARTIAL | 用户启动日志确认原先存在两个 Bean；已排除自动配置，后端编译/5 个测试通过，完整服务重启验证仍待可用基础设施 |
| H5 路由刷新保留 | PARTIAL | 代码逻辑已修复；浏览器可打开登录页，但未登录且后端不可用，无法完成有效会话刷新 |
| 后端重启后会话恢复 | BLOCKED | 本机当前没有可用 MySQL/Redis，未执行真实登录、重启和续会话联调 |
| 数据库变更 | PASS | 本次无数据库变更 |
