# 数据库

本次复用已完成的 `V1__init_schema.sql` 和 `V2__create_app_file_table.sql`，不修改历史 Migration。

- `app_user`：H5 账号、密码哈希、状态和最近登录信息。
- `app_login_log`：成功/失败登录审计，不保存密码或 Token。
- `app_file`：用户归属、MinIO Bucket/Object Key、上传状态和文件元数据。

实现中 Entity 与 SQL 字段逐项对应；`deleted` 过滤由查询条件和 MyBatis-Plus 逻辑删除共同保障。
