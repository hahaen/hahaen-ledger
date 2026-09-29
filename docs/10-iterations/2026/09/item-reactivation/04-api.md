# API

`POST /api/app/items/{id}/reactivate` 请求 `{ "idempotencyKey": "..." }`，响应沿用 `ItemVO`。无当前用户归属、已删除返回 `ITEM_NOT_FOUND`；在役物品的新请求键及已变化状态的旧请求键返回 `IDEMPOTENCY_CONFLICT`。同一物品同键在仍在役时重试只读返回，不重复写入。字段形状和其他接口保持原契约。
