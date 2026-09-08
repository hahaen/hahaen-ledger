# API

新增：`PUT /api/app/accounts/{id}/order`。

请求字段：`targetAccountId`、`expectedSortOrder`、`targetExpectedSortOrder`、`idempotencyKey`。接口只允许当前用户的同类有效账户换序，成功返回当前用户账户列表；跨用户、跨类型、已发生顺序冲突均返回业务错误。

账户列表、账户详情和资产概览的账户对象新增 `sortOrder`。
