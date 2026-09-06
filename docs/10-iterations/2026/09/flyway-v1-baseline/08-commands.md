# 命令与结果

以下命令均在本次实际执行，未记录凭证或完整连接串：

1. `mvn -q "-Dledger.audit.dev=true" "-Dtest=AuditFieldDefaultsDevTest" test`：PASS，收口前旧 V4 真实 DEV 复核。
2. `mvn -q "-Dledger.audit.dev=true" "-Dledger.dev.reset=true" "-Dtest=AuditFieldDefaultsDevTest" test`：首次因 Maven 增量 `target/classes` 残留旧 V2 资源而 FAIL；未修改配置。
3. `mvn -q clean`：PASS，清理构建产物。
4. `mvn -q "-Dledger.audit.dev=true" "-Dledger.dev.reset=true" "-Dtest=AuditFieldDefaultsDevTest" test`：PASS，DEV clean → V1 migrate → validate 与完整 Schema 回归。
5. `mvn -q "-Dledger.audit.dev=true" "-Dtest=AuditFieldDefaultsDevTest" test`：PASS，重建后重复 migrate/validate 无冲突。
6. `mvn -q test`：PASS，普通后端测试通过，DEV 集成测试按设计跳过。
7. `mvn -q clean package "-Dledger.audit.dev=true"`：PASS，后端构建成功，含 V1 DEV 集成校验。
8. `mvn -q spring-boot:run "-Dspring-boot.run.arguments=--server.port=0"`：应用启动 PASS；日志确认 dev Profile、Flyway 已校验 1 个 migration、schema version 1、Redis PING 和 MinIO Bucket 可用；随后人工中断并正常优雅停机。
