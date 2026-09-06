# 回滚

本次未在数据库执行 Migration。若尚未执行，仅需删除 `V2__create_app_file_table.sql` 和本迭代目录。

若已在共享环境执行，不得修改或删除历史 Migration；应新增后续 Migration 删除外键、表或字段，并先确认文件元数据及 MinIO 对象是否需要迁移或清理。
