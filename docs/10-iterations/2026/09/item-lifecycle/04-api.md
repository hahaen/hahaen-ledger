# API

GET /api/app/items?status=ACTIVE|RETIRED|ALL&page=1&pageSize=20：摘要、分页列表。GET /api/app/items/{id}：物品及历史成本点。POST /api/app/items：name, priceCents, purchasedOn, serving, retiredOn?, resaleCents?, idempotencyKey。POST /api/app/items/{id}/retire：retiredOn, resaleCents, idempotencyKey。DELETE /api/app/items/{id}?idempotencyKey=...。全部强制当前身份，ID以字符串返回。
