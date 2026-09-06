# 验证

| 项目 | 状态 | 证据 |
| --- | --- | --- |
| V2 Migration 文件生成 | PASS | `server/src/main/resources/db/migration/V2__create_app_file_table.sql` 已生成 |
| MinIO 对象元数据字段 | PASS | 已包含 Bucket、Object Key、MIME、大小、摘要和状态 |
| 用户归属和头像外键 | PASS | `user_id` 必填，并为 `app_user.avatar_file_id` 增加外键 |
| 账本/账单归属字段 | PARTIAL | 字段和业务类型约束已生成，待对应业务表生成后补充外键 |
| 业务上传与预览接口 | NOT_RUN | 本次未实现 |
| Flyway/MySQL 执行 | NOT_RUN | 当前未执行数据库迁移 |
