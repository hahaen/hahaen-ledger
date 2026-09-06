# 命令记录

只记录实际执行且不含凭证的命令。当前已执行：

- 读取用户提供的启动日志：确认 Flyway V3 validation failure。
- 读取 V1/V2/V3 和项目配置：完成静态核对。

数据库核查、修复、测试和启动命令已实际执行，结果如下：

- 通过 JDBC 只读核查 `flyway_schema_history` 和 `information_schema`：发现 V3 失败且只留下空 `app_user` 的新增列。
- 修正 `V3__standardize_audit_columns_and_comments.sql` 中 6 处列注释与表注释之间缺失的逗号。
- 通过 JDBC 定向清理空 `app_user` 上一次失败迁移留下的 8 个临时列。
- 通过 Flyway Java API 执行 `repair`：移除失败迁移记录。
- 通过 Flyway Java API 执行 `migrate`：V3 成功应用。
- `mvn test`：BUILD SUCCESS，2 tests，0 failures/errors。
- `mvn clean package`：BUILD SUCCESS。
- `mvn spring-boot:run`：Flyway 校验通过，应用、Redis、MinIO 均启动/探测成功。
