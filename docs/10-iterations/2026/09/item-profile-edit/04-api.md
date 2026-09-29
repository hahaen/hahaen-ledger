# API

新增 `PUT /api/app/items/{id}`，请求字段为 `name`、`priceCents`、`purchasedOn`、`idempotencyKey`，响应复用 `ItemVO`。当前登录用户决定归属；未知、跨用户或已删除 ID 统一返回 `ITEM_NOT_FOUND`。名称、金额和日期沿用物品创建规则，并要求购买日期不晚于既有退役日期。幂等键按用户唯一；同键同内容重试成功，同键异内容返回 `IDEMPOTENCY_CONFLICT`。
