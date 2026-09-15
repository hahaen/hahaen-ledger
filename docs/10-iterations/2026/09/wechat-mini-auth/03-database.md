# 数据库

本迭代不新增或修改 Migration。

- `user_identity` 已有 `provider`、`open_id`、`union_id`、`deleted` 和 `(provider, open_id)` 唯一约束。
- `app_user.login_account` 与 `password_hash` 可同时为空，满足微信用户首次静默登录。
- `app_login_log.login_channel` 已允许 `WECHAT_MINI_PROGRAM`。
- 微信 `session_key` 不落库。

实现后需核对 Entity、Mapper、V1 基线和目标数据库结构一致；若共享环境缺少现有表，必须先按 Flyway 基线审计，不得手工补表。
