# 测试

验证重点：

- DEV catalog 必须为 `haji_dev`。
- Flyway clean 后从空库 migrate V1，并通过 validate。
- history 只有一个成功的 SQL 版本 V1。
- 六张表、全部字段 COMMENT、53 个公共/历史审计列、索引、8 个外键和 6 个 CHECK 约束符合预期。
- 六表默认插入、显式 `deleted=NULL`、`created_at=NULL` 拒绝和事务回滚通过。
- `AuditMetaObjectHandlerTest`、`MoneyUtilsTest`、后端测试和打包通过。
