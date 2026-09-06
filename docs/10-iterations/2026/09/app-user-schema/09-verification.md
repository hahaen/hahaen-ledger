# 验证

| 项目 | 状态 | 证据 |
| --- | --- | --- |
| Migration 文件生成 | PASS | `server/src/main/resources/db/migration/V1__init_schema.sql` 已生成 |
| `app_user` 字段与审计规则 | PASS | 已按项目公共字段规则编写，并增加 `last_login_ip` |
| `login_account` 全表唯一 | PASS | 唯一键不包含 `status`、`deleted` |
| `user_identity` 跨端身份关联 | PASS | 已包含 `provider + open_id` 唯一约束及用户外键 |
| `app_login_log` 登录审计表 | PASS | 已记录渠道、结果、用户、IP、失败码和 Trace ID |
| Flyway 默认配置 | PASS | 已配置迁移位置、基线、校验、顺序和禁止清库策略 |
| Flyway/MySQL 执行 | NOT_RUN | 当前未执行数据库迁移 |
| Entity/Mapper/Service/API | NOT_RUN | 本次按要求暂不实现 |
| 前端绑定流程 | NOT_RUN | 本次按要求暂不实现 |
