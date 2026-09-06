# 验证

| 项目 | 状态 | 证据 |
| --- | --- | --- |
| DEV 目标确认 | PASS | 测试在 Flyway clean 前通过 JDBC `connection.getCatalog()` 断言仅为 `haji_dev` |
| 从空库执行 V1 | PASS | `AuditFieldDefaultsDevTest` 使用显式 reset 完成 clean、migrate、validate |
| Flyway history | PASS | `flyway_schema_history` 仅有一个成功 SQL 版本 `1` |
| 六张业务表 | PASS | information_schema 表集合及 13/8/16/18/21/16 字段数核对 |
| 字段默认值/NULL/COMMENT | PASS | 53 个公共/历史审计列及全部字段 COMMENT 核对；created_at/default、deleted/default 与 NULL 行为通过 |
| 索引/外键/CHECK | PASS | 预期索引集合、8 个外键、6 个 CHECK 约束全部匹配 |
| Entity/Mapper | PASS | 六个 Entity 的 `@TableName`、继承关系与六个 `BaseMapper` 静态核对；测试初始化通过 |
| 自动填充和金额测试 | PASS | `AuditMetaObjectHandlerTest`、`MoneyUtilsTest` 通过 |
| 后端完整测试/构建 | PASS | `mvn -q test`、`mvn -q clean package "-Dledger.audit.dev=true"` 均成功 |
| 应用启动/Flyway 冲突 | PASS | `mvn spring-boot:run --server.port=0` 启动成功；日志确认 dev、Flyway 1 migration、schema version 1、正常停机 |
| 配置未修改 | PASS | 本次仅改 Migration、测试、文档；配置文件哈希前后保持一致 |

本次数据库收口范围无 FAIL/BLOCKED。Redis、MinIO、前端和真实业务 HTTP 不在本次验证范围内，不据此宣称 PASS。
