# 数据库

本次不新增、不修改数据库表、字段、索引或 Flyway Migration。

认证会话从后端 JVM 内存迁移到 Redis，业务数据仍由现有 MySQL Schema 管理。Redis 使用现有配置连接，不执行清库操作。
