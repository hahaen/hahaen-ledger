# 设计

以当前 V4 执行后的最终 Schema 为事实来源，将原 V2 的外键/CHECK 约束、V3 的字段和中文注释、V4 的公共字段可空与默认值直接写入 `V1__init_schema.sql`。

Migration 目录只保留 V1。DEV 重建通过显式 `ledger.dev.reset=true` 调用 Flyway clean，再 migrate、validate；代码在 clean 前断言 JDBC catalog 为 `haji_dev`。不手工修改 `flyway_schema_history`，不使用手工 ALTER 冒充迁移。
