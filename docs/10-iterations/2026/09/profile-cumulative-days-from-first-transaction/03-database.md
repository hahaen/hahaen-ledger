# 数据库

不新增 Migration，也不修改现有表。

- 数据源：`transaction_detail.occurred_at`。
- 过滤条件：`user_id = 当前会话用户` 且 `deleted = 0`。
- 既有 `idx_transaction_detail_user_occurred (user_id, occurred_at, deleted)` 可供当前用户范围查询使用。

数据风险：历史账单可被回填为更早的业务日期，下一次读取个人资料时累计天数会随之调整；这是采用业务发生日期作为统计起点的预期结果。真实 MySQL 的执行计划和索引核对未在本次运行。
