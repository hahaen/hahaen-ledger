# 设计

新增只读接口 `GET /api/app/home/recent-transactions?beforeMonth=YYYY-MM`。未传游标时以业务时区当前月为结束月，返回该月及其前一月；传入 `beforeMonth` 时，该月作为排他上界，返回其前两个自然月。响应携带本段 `startMonth`、`endMonth`、账单和 `hasMore`。

前端首次加载摘要与首段记录；滚动触底以 `startMonth` 作为下一次的 `beforeMonth`，把新账单按 ID 去重后追加。摘要缓存继续仅用于当前月；列表数据单独维护，避免把两种查询语义混合。
