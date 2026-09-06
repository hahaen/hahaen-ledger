# 验证

验证证据：

- `flyway_schema_history`：V1/V2/V3 均 `success=true`。
- `information_schema`：6/6 表有表注释；主表审计字段 50/50；关联表 `user_identity.deleted` 存在；6/6 删除标识为 NOT NULL DEFAULT 0。
- Spring Boot：Flyway 成功校验 3 个迁移，提示 schema version 3，Tomcat 监听 8080，应用完成启动。
- 基础设施：Redis PING succeeded；MinIO bucket `haji-dev` available。
- Maven：测试和打包均 BUILD SUCCESS。

状态：PASS（本次 Flyway 启动故障）。
