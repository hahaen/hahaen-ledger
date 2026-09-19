# 04｜API

- 账单创建/编辑接口结构不变。`EXPENSE` 的 `accountId` 可指向当前用户的资金账户或信贷账户；`INCOME` 仍要求资金账户。
- 账户 API 字段不变：资金账户 `balanceCents` 可为负；信贷账户 `currentDebtCents` 可为负表示溢缴，也可高于 `creditLimitCents`。
- 金额仍为整数分；账单金额仍须为正数。
- 转账和还款不再因资金账户余额不足而返回错误；信贷支出超过可用额度仍成功保存，前端负责显示超额提示。
- Sa-Token 当前用户归属过滤、幂等键及 HTTP 错误格式未变。
