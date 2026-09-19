# 数据库

无 Migration、字段、索引或约束变化。`app_file.object_key` 已是稳定 MinIO 对象定位符，长度为 `VARCHAR(512)`，新格式仍符合其用途。

历史 `object_key` 与 `app_user.avatar_file_url` 中的旧值保持不变；无需重写数据库，也不批量改名 MinIO 对象。
