# 数据库核对

## 已确认事实

- Migration 当前为 V1 用户/身份/登录日志、V2 文件元数据、V3 统一资产账户、V4 账单明细与退款。
- `AssetAccount`、`TransactionDetail`、`TransactionRefund` 使用 `BaseAuditEntity`；V3/V4 字段名与 Entity 的驼峰映射一致。
- 当前没有 `app_book` 表；账单和账户通过 `user_id` 归属用户，不能把“单账本”写成独立账本表或账本 ID 过滤。
- V3 数据库没有账户名称唯一索引，但 `AccountService` 对当前用户的有效账户执行重名检查；规范已区分数据库约束和 Service 约束。

## 未确认事实

本次未连接 MySQL，因此没有 `flyway_schema_history`、`information_schema.columns/statistics/table_constraints` 或真实 CHECK/外键执行证据。Migration 文件静态存在标记为 PASS（静态），目标数据库实际状态为 `BLOCKED`，不能合并为一个结论。

