# 后端

Home 域新增 `HomeRecentTransactionsVO` 与 `recentTransactions()`；交易域提供按时间段查询和“段前是否有记录”判断。Mapper 的所有条件均包含 `user_id` 与 `deleted = 0`，排序为 `occurred_at DESC, id DESC`。摘要 Service 保持只按当前月计算。
