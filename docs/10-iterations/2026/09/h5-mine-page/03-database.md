# 数据库

本轮不新增 Migration。

复用并核对：

- `app_user.created_at`：用户创建时间，作为累计记账天数唯一数据来源。
- `app_user.avatar_file_id`：当前头像文件元数据 ID。
- `app_file`：头像对象的用户归属、MinIO Bucket/Object Key、状态和逻辑删除字段。

后端查询必须按当前 Sa-Token 用户过滤，并排除逻辑删除数据。若后续发现共享环境未执行现有 `V2__create_app_file_table.sql`，应按项目规则先记录环境 BLOCKED，不手工补表。
