# 数据库

## Migration

- 文件：`server/src/main/resources/db/migration/V4__create_transaction_detail_and_refund_tables.sql`
- 新增表：`transaction_detail`、`transaction_refund`。
- V4 未修改 V1、V2、V3。

## 核心字段

- 主表：`transaction_no`、`transaction_type`、`original_amount`、`amount`、`has_refund`、`account_id`、`from_account_id`、`to_account_id`、`occurred_at`。
- 退款表：`refund_no`、`transaction_id`、`refund_amount`、`idempotency_key`、`created_at`、`deleted` 及删除审计字段。
- 所有金额是整数分；两张表均使用完整 10 个公共审计字段。账单按 `user_id` 归属当前唯一账本，不重复保存 `book_id`；退款有独立编号、金额、幂等键和逻辑删除生命周期，不属于纯关系表。

完整字段、索引、约束和需求映射见 `docs/02-database/transaction-detail-refund.md`。
