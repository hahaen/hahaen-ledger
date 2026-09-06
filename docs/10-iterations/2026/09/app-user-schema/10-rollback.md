# 回滚

本次未在数据库执行 Migration。若尚未执行，仅需删除本次新增的 Migration 文件和迭代目录。

若后续已在共享环境执行，不得修改或删除历史 Migration；应新增回滚用途的后续 Migration，并先评估 `app_user`、`user_identity` 数据是否已被业务使用。
