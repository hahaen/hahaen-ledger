# 03｜数据库

- 新增 `V5__allow_signed_account_balances.sql`。
- 删除 V3 的金额非负约束并重建账户类型金额形状约束；仅要求信贷额度非负，资金余额和信贷欠款允许为负，且欠款允许超过总额度。
- 不修改 V1–V4，不增加字段、索引或表。
- `AssetAccount.balanceCent`、`currentDebtCent` 均为 Java `Long`，可承载有符号 BIGINT；API 金额单位仍为整数分。
- 迁移执行、Flyway 历史、MySQL `information_schema` 状态待实际 DEV 数据库验证；迁移文件存在不代表数据库已执行。
