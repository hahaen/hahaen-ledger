# 业务表分类

| 表名 | 中文名称 | 类型 | 业务说明 | 公共字段规范 |
| --- | --- | --- | --- | --- |
| `app_user` | 应用用户表 | 主表 | 用户生命周期和资料 | 主表 10 字段 |
| `ledger_book` | 账本表 | 主表 | 用户账本及统计时区 | 主表 10 字段 |
| `ledger_account` | 账本账户表 | 主表 | 资金账户或信贷账户 | 主表 10 字段 |
| `ledger_transaction` | 账单流水表 | 主表 | 支出、收入、转账、还款 | 主表 10 字段 |
| `transaction_refund` | 账单退款记录表 | 主表 | 可独立删除和审计的退款业务记录 | 主表 10 字段 |
| `user_identity` | 用户第三方身份关联表 | 关联表 | 用户与微信身份提供方的关系 | `created_at + deleted` |

判定依据是业务生命周期和实际关联意义，不以字段数量代替判断。当前没有文件业务表，MinIO 仅保留服务适配器，凭证 UI 未开放。

全部表公共字段仅 `created_at` 为 NOT NULL DEFAULT CURRENT_TIMESTAMP(3)，`deleted` 为 NULL DEFAULT 0；主表其余公共字段均可空。关联表历史 `updated_at` 保留且可空，不加入 BaseRelationEntity。当前约束见 V1。
