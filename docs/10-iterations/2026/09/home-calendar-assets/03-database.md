# 03｜数据库

本次数据库结构状态：`NOT_CHANGED`。

只读使用现有表：

- `asset_account`：账户类型 `FUND`/`CREDIT`，资金余额 `balance_cent`，信贷额度 `total_limit_cent`、欠款 `current_debt_cent`，净资产标识 `include_net_asset`，当前用户 `user_id`，逻辑删除 `deleted`。
- `transaction_detail`：账单类型、原始/有效金额、账户关系、发生时间、备注、幂等键和审计字段。
- `transaction_refund`：退款父账单、退款金额、幂等键和审计字段。
- `app_user`：当前用户存在性和状态校验。

未修改 `server/src/main/resources/db/migration/V1__init_schema.sql`、V2、V3、V4；未新增 Migration；未执行 DDL。由于没有真实 MySQL 时不能宣称 `information_schema` 运行时验证通过，最终报告单列实际状态。

