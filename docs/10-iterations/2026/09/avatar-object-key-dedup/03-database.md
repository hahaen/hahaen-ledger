# 数据库

新增 `V6__replace_avatar_file_id_with_object_key.sql`：添加 `app_user.avatar_file_url VARCHAR(512)`，把旧关联的已就绪头像对象 Key 回填后删除 `fk_app_user_avatar_file` 和 `avatar_file_id`。同时添加 `app_file(user_id, business_type, file_hash, status, deleted)` 索引。

迁移仅回填未删除的 `READY` 头像；未完成、失败或删除文件不会成为当前头像。共享环境不得修改 V1–V5，需先确认 `flyway_schema_history` 与 `information_schema` 后执行 V6。
