# 数据库

## Migration

- 文件：`server/src/main/resources/db/migration/V1__init_schema.sql`
- 建立表：`app_user`、`user_identity`、`app_login_log`
- `app_user` 使用主表完整审计字段，`login_account` 全表唯一，增加 `last_login_ip`。
- `user_identity` 使用关联表字段，仅保留 `created_at` 和 `deleted`。
- `app_login_log` 使用完整审计字段，按用户、账号、结果和创建时间建立查询索引。
- 文件表 V2 Migration 为 `app_user.avatar_file_id` 增加外键约束。
- 主键由 MyBatis-Plus `ASSIGN_ID` 生成，数据库不使用自增。

## Flyway 默认配置

默认配置位于 `server/src/main/resources/application.yml`：

- 迁移位置：`classpath:db/migration`。
- 默认启用 Flyway，并允许对非空开发库执行基线。
- 迁移前校验开启。
- 禁止乱序迁移和 `clean` 清库。
- 关键配置支持使用 `FLYWAY_*` 环境变量覆盖。

## 约束

- `app_user.login_account` 唯一。
- `user_identity(provider, open_id)` 唯一。
- `app_login_log` 的登录渠道仅允许 `H5_PASSWORD`、`WECHAT_MINI_PROGRAM`。
- `app_login_log` 的登录结果仅允许 `SUCCESS`、`FAILURE`，失败结果必须有失败编码。
- 用户状态仅允许 `ACTIVE`、`DISABLED`。
- H5 账号和密码哈希必须同时为空或同时存在。
- 身份记录必须关联有效的 `app_user` 主键。
