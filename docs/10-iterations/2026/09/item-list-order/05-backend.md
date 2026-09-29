# 后端

修改 `PersonalItemMapper.listOwned` SQL 的 `ORDER BY`，加入 ACTIVE 优先级，其后按购买日期和 ID 倒序。查询仍限制当前用户、有效用户及未删除物品。Service 筛选和分页逻辑不变。
