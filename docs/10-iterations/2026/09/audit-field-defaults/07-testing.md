# 测试

- PASS：`AuditMetaObjectHandlerTest` 六项单元测试。
- 历史 PASS：`AuditFieldDefaultsDevTest` 当时的真实 DEV 集成测试包含 Flyway V4、53 个列定义、六表默认插入/显式 NULL、created_at 非空与测试行回滚；当前同一验证已收口到 V1，最新证据见 `../flyway-v1-baseline/09-verification.md`。
- PASS：既有 `MoneyUtilsTest` 两项金额测试。
- PASS：`mvn clean package "-Dledger.audit.dev=true"`，9 tests，0 failures/errors/skipped，BUILD SUCCESS。
- PASS：打包 JAR 的 DEV 启动、Redis PING 与 MinIO Bucket 探测；临时启动进程已停止。

首次 `mvn test` 有两项失败：Mockito 对 Long 默认返回 0，而测试预期匿名 NULL；显式桩设 optionalId=null 后全部通过。该失败是测试数据设置问题，不是数据库或生产逻辑失败。

本次完整业务 HTTP、全新空库安装、前端构建/微信平台 NOT_RUN。
