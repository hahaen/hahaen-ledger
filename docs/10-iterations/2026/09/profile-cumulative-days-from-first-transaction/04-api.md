# API

接口路径和 JSON 字段不变：`GET /api/app/user/profile` 的 `cumulativeDays: long`。

语义从“账号创建日起算”变为“最早有效账单业务日期起算”。没有有效账单时返回 `0`，而不是用账号创建时间补算。接口仍由 Sa-Token 当前用户确定范围，不接受前端传入的用户 ID。
