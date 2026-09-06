# 数据库

## Migration

- 文件：`server/src/main/resources/db/migration/V2__create_app_file_table.sql`
- 新增表：`app_file`
- 为 `app_user.avatar_file_id` 增加文件外键。
- `book_id`、`transaction_id` 暂不增加外键，待账本和账单表创建后补充，避免跨 Migration 引用不存在的表。

## 主要字段

- `bucket_name`：MinIO Bucket 名称。
- `object_key`：MinIO 对象 Key，稳定保存，不等同于访问链接。
- `content_type`、`file_size`、`file_hash`：文件校验和展示元数据。
- `status`：`UPLOADING`、`READY`、`FAILED`、`DELETING`、`DELETED`。
- `business_type`：`AVATAR`、`TRANSACTION_ATTACHMENT`。
