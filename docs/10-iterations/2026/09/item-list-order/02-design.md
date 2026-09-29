# 设计

在 Mapper 的全量用户物品查询中按 `CASE status` 将 ACTIVE 排在 RETIRED 前，再按 `purchased_on DESC, id DESC` 排序。Service 先取得该有序列表，再执行状态筛选和分页，因此所有页共享同一个排序结果。

日期降序等价于同组内购入日期较新的物品优先；ID 倒序用于购买日期相同时的稳定顺序。没有新增字段、索引或接口参数。
