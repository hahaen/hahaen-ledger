# 后端

- `AccountVO`、`TransactionVO`、`RefundVO` 以及认证、个人资料、文件响应 VO 的 ID 改为 `String`。
- Service 在 API 边界使用 `String.valueOf` 转换，空的关联 ID 保持 `null`。
- 账户和流水的实体查询、归属校验、事务、删除和金额计算保持原实现。
