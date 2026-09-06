# 数据库

新增 `V3__standardize_audit_columns_and_comments.sql`。主表为 `app_user`、`ledger_book`、`ledger_account`、`ledger_transaction`、`transaction_refund`；`user_identity` 为关联表。Migration 先补可空字段、回填已有数据，再收紧 `created_by`，并补齐 COMMENT/TABLE_COMMENT。

2026-09-06 后续规则调整：上述为 V3 历史设计，当时约束由 V4 替代；后续 `flyway-v1-baseline` 又将 V4 最终状态吸收到唯一 V1 基线。公共字段仅 created_at 必填且默认当前时间，created_by/updated_at/deleted 等可空，deleted 默认 0。当前真实 DEV 基线验证详见 `../flyway-v1-baseline/03-database.md`。
