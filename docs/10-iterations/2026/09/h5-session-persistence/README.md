# `h5-session-persistence`｜H5 刷新保留页面与会话持久化

本次迭代修复 H5 刷新后被强制送回首页或登录页的问题：浏览器刷新后保留当前 hash 路由，服务端 Sa-Token 会话写入 Redis，并统一使用 `haji:` Key 前缀。

状态以 `09-verification.md` 为准。真实浏览器刷新和后端重启后的连续会话验收必须以实际启动的 H5、MySQL、Redis 环境证据为准。

## 文件导航

- `01-requirement.md`：需求和验收条件。
- `02-design.md`：前后端设计。
- `03-database.md`：数据库影响。
- `04-api.md`：API 影响。
- `05-backend.md`：Sa-Token Redis 会话实现。
- `06-frontend.md`：H5 启动恢复实现。
- `07-testing.md`：测试范围。
- `08-commands.md`：真实执行命令和结果。
- `09-verification.md`：最终验证状态。
- `10-rollback.md`：回滚策略。
