# 后端

- 新增微信 code2Session 客户端和微信小程序认证 Service。
- `AuthController` 新增微信登录入口，H5 入口不变。
- 首次登录创建默认昵称为“账本主人”的本地用户。
- 登录成功写入 `WECHAT_MINI_PROGRAM` 成功日志并设置 Sa-Token 会话审计名称。
- 失败记录使用 `LoginAuditService`，不记录 code、AppSecret、session_key 或 open_id。
- 用户查询显式过滤 `status=ACTIVE`、`deleted=0`。

事务覆盖本地用户与身份关联的首次创建；唯一键冲突时回滚本次创建并要求客户端重新获取 code 重试。
