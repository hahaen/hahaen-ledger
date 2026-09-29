# 后端

`ItemController` 新增重新服役入口。`ItemService` 在事务中锁当前用户与所属物品，检查状态和已用请求键；`PersonalItemMapper` 用显式 SQL 将 `status` 改为 `ACTIVE`，将 `retired_on`、`resale_cent` 改为 NULL，并更新审计字段。同事务写入 V8 请求关系表。保留最后一次退役键，阻止重新服役后旧退役请求重放。费用视图沿用 `ItemCosts` 的在役计算。
