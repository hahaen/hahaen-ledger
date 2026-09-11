# 后端

- `TransactionDetailMapper` 新增最早有效业务记账时间查询，条件固定为 `user_id` 和 `deleted=0`。
- `ProfileService` 先取得当前用户并保持原有 ACTIVE/逻辑删除校验，再查询最早有效账单时间。
- `calculateCumulativeDays` 改为接收最早记账日：无记录或未来日期返回 0，其他情况按自然日含首日计算。

本次为只读聚合，不涉及金额、账户余额、事务写入、缓存或文件链路。
