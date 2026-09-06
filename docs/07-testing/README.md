# 测试与验收规范

后端以 `mvn test`、`mvn clean package` 和 DEV 启动验证为准；前端以 `pnpm install`、实际存在的 typecheck/build script 为准。真实验收要覆盖权限、IDOR、金额、事务、幂等、软删除、统计、Redis、MinIO、微信平台和设计稿状态。没有执行就标记 `NOT_RUN`，外部条件不可得才标记 `BLOCKED`。

公共字段回归包含 `AuditMetaObjectHandlerTest` 的六项单元测试。`AuditFieldDefaultsDevTest` 默认跳过；仅在明确需要迁移本地配置的 `haji_dev` 时运行 `mvn test "-Dledger.audit.dev=true"`，验证 V4、六表 information_schema 与事务回滚的默认值插入，不输出凭证。
