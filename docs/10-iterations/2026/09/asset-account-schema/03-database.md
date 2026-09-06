# 数据库

## Migration

- 文件：`server/src/main/resources/db/migration/V3__create_asset_account_table.sql`
- 新增表：`asset_account`。
- V1/V2 为历史迁移，本次未修改。
- `user_id` 外键关联 `app_user.id`；当前单用户、单账本模型不在账户表重复保存账本字段。

## 关键约束

- `account_type=FUND` 时，`balance_cent` 必填，额度和欠款必须为空。
- `account_type=CREDIT` 时，额度和欠款必填，余额必须为空，且当前欠款不得超过总额度。
- 金额单位为分，均非负；`include_net_asset` 仅允许 `0/1`。
- 账户名称非唯一；后续业务展示、选择和关联必须使用账户ID区分账户。
- 表使用完整 10 个公共审计字段，`deleted` 遵循 `NULL DEFAULT 0` 规范。
