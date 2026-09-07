# 用户与文件表现状

本文件补充 V1/V2 的当前数据库入口，正式字段仍以 Migration 为准。

## V1 用户与身份

### `app_user`

用户主表，保存 H5 认证资料、昵称、头像文件 ID、状态、最近登录信息和完整 10 个公共审计字段。

- `login_account` 全表唯一，允许仅微信身份用户为空。
- `password_hash` 与 `login_account` 必须同时为空或同时存在；只保存服务端哈希。
- `status` 为 `ACTIVE` 或 `DISABLED`；业务查询还必须过滤 `deleted=0`。
- `avatar_file_id` 在 V2 通过外键关联 `app_file.id`。

### `user_identity`

第三方身份关联表，当前使用 `provider` + `open_id` 唯一约束，只有 `created_at` 和 `deleted` 两个公共关联字段。`open_id` 不返回前端，也不作为日志内容。

### `app_login_log`

登录审计主表，记录渠道、结果、账号快照、用户、IP、User-Agent、失败编码和 Trace ID；不保存密码、Token 或微信 `open_id`。

## V2 `app_file`

文件元数据主表使用完整 10 个公共审计字段。保存用户归属、业务类型、Bucket、Object Key、原始文件名、MIME、大小、SHA-256、ETag、状态和幂等键；不保存永久 URL。

- `business_type` 枚举包含 `AVATAR` 和预留的 `TRANSACTION_ATTACHMENT`。
- 头像必须属于用户，`book_id`/`transaction_id` 为空；账单附件字段组合由 Schema 约束预留，但当前业务未开放。
- 文件状态包括 `UPLOADING`、`READY`、`FAILED`、`DELETING`、`DELETED`；状态与数据库 `deleted` 是两套语义。
- 当前代码只允许通过 `AppFileService` 创建 `AVATAR`，完成、预览和删除均再次校验当前用户。

V1/V2 是否已在目标环境执行，不能从本文件或 Migration 文件存在推断；必须查询 Flyway history 和 `information_schema`。
