# 验证

| 范围 | 状态 | 证据 |
| --- | --- | --- |
| Flyway V4 和真实 Schema | PASS | AuditFieldDefaultsDevTest，haji_dev，Flyway validate + 当前版本 4，53 列可空/默认/注释 |
| 六表默认值与可空行为 | PASS | 六表默认插入、deleted 显式 NULL、created_at 拒绝 NULL，测试数据全部回滚 |
| Java 自动填充 | PASS | AuditMetaObjectHandlerTest 6 tests，0 failures/errors |
| 金额回归 | PASS | MoneyUtilsTest 2 tests，0 failures/errors |
| 后端打包 | PASS | mvn clean package，9 tests，0 failures/errors/skipped，BUILD SUCCESS |
| DEV 运行 | PASS | target/audit-defaults-startup.log：Started、Redis PING、MinIO Bucket；临时进程已停止 |
| DTO/VO/TypeScript 兼容核对 | PASS | DTO/VO 无新增审计参数；app/src/stores/ledger.ts 未消费公共审计字段 |
| 全量业务/设计验收 | PARTIAL | 本次完成公共审计字段范围；完整 HTTP、设计及微信平台未执行 |
| 全新空库安装/前端构建 | NOT_RUN | 本次执行已有 DEV 从 V3 升级 V4，无前端代码改动 |

当前范围无未解决 FAIL 或外部 BLOCKED。初次单元测试桩问题已修复，保留在 commands/testing 中。真实生产环境未部署，不以 DEV 结果替代其他环境执行状态。
