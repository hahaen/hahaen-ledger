# 回滚说明

V4 尚未执行时，可在部署前移除本次新增 V4 文件并重新生成待部署包；不得修改或删除 V1–V3。

V4 一旦在共享环境执行，不通过修改历史文件回滚。先评估 `transaction_detail`、`transaction_refund` 数据和外键依赖，再新增后续版本迁移执行可审计的反向操作；默认保留业务数据并使用逻辑删除/停用。正式 Flyway Migration 不加入 `DROP TABLE` 或自动回滚逻辑。
