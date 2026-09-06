# 数据库

`V1__init_schema.sql` 一次性创建六张业务表：`app_user`、`user_identity`、`ledger_book`、`ledger_account`、`ledger_transaction`、`transaction_refund`。

基线包含全部业务字段、主键、唯一/普通/联合索引、8 个外键、6 个 CHECK 约束、中文字段 COMMENT 和表 COMMENT。五张主表使用 10 个公共审计字段；`user_identity` 保留历史 `updated_at`，Entity 仍只映射 `created_at` 与 `deleted`。`created_at` 为 `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)`，其余公共字段允许 NULL，`deleted` 为 `TINYINT NULL DEFAULT 0`。当前无必要初始化业务数据。
