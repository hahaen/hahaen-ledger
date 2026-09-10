# V5 历史迁移校验失败修复

## requirement
用户提供启动日志：Flyway V5 checksum mismatch，数据库为 266153221，本地为 2076277146，阻断 Spring 容器启动。

## design
定位到 V5 首行注释之前误加 `cd`。仅移除这两个字符，使文件恢复已执行版本的校验和，不执行 repair、不关闭 validate、不修改业务 SQL。

## database
不变更 Schema 或 flyway_schema_history。恢复既有迁移内容而非重新设计历史迁移。

## api / backend / frontend
接口及前端无变化；修复后端启动时迁移资源校验。补充历史迁移完整性回归。

## testing
核对恢复后的 Flyway 行级 CRC32 为数据库已有值，执行后端测试、打包和可用环境启动验证。

## commands / verification
- PASS：修复前 2076277146，移除首行 cd 后 266153221，与用户日志中数据库已执行值完全一致。
- PASS：server 目录执行 `mvn test package`，25 项测试，0 failures/errors/skipped，JAR 打包成功。
- PASS：`java -jar target/hahaen-ledger-server-1.0.0.jar --server.port=18080` 使用现有 DEV 配置启动；实际日志为 Successfully validated 5 migrations、Current version 5、Schema is up to date / No migration necessary、Started LedgerApplication in 2.681 seconds。
- PASS：GET http://127.0.0.1:18080/api/app/accounts 未登录返回 401，服务正常响应。
- 验证结束已停止临时 18080 实例。可在 IDEA 重新运行 LedgerApplication；target/classes 和 JAR 均已同步恢复后的 V5。
- NOT_RUN：本次不涉及已登录账务写入回归。本次启动证据不代表其他尚未验证的业务已通过。

## rollback
无数据库回滚。不要恢复错误前缀或通过禁用校验绕过错误。
