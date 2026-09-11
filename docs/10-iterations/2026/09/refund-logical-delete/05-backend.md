# backend

`TransactionRefundMapper.softDeleteById` 显式更新逻辑删除标识和审计字段，避免 `BaseMapper.updateById` 对 `@TableLogic` 字段的普通更新排除行为。`TransactionService` 的单笔退款删除与账单删除级联路径均改用此方法；单笔删除后按有效退款金额回算 `hasRefund`，没有有效退款时设为 0。
