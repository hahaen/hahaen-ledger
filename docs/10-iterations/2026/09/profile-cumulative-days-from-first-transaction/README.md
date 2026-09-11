# `profile-cumulative-days-from-first-transaction`｜累计记账天数口径修正

日期：2026-09-11。

“我的”页的累计记账天数不再从账号创建日开始计算，而是从当前用户最早一笔未逻辑删除账单的业务记账日开始计算；首日计为第 1 天，没有有效账单时显示 0 天。

本次不修改页面展示字段、数据库结构或 Flyway，只修正 `GET /api/app/user/profile` 返回字段 `cumulativeDays` 的服务端统计来源。

| 文档 | 内容 |
| --- | --- |
| [01-requirement.md](01-requirement.md) | 需求与验收 |
| [02-design.md](02-design.md) | 统计口径与边界 |
| [03-database.md](03-database.md) | 表、索引与数据风险 |
| [04-api.md](04-api.md) | 接口契约 |
| [05-backend.md](05-backend.md) | Mapper、Service 与权限 |
| [06-frontend.md](06-frontend.md) | 页面影响 |
| [07-testing.md](07-testing.md) | 测试范围 |
| [08-commands.md](08-commands.md) | 实际命令 |
| [09-verification.md](09-verification.md) | 验证状态 |
| [10-rollback.md](10-rollback.md) | 回滚策略 |
