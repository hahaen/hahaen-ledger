# 长期迭代档案规范

目录按 `YYYY/MM/<feature-key>/` 分层；`feature-key` 使用小写短横线，只描述一个独立需求。重大功能、Bug、数据库、权限、基础设施、设计整改必须先创建 iteration；纯文案且不影响规范的修改可以不单独创建。

每个 iteration 固定包含：`README.md`、`01-requirement.md`、`02-design.md`、`03-database.md`、`04-api.md`、`05-backend.md`、`06-frontend.md`、`07-testing.md`、`08-commands.md`、`09-verification.md`、`10-rollback.md`。commands 只记录真实执行过的命令和结果，禁止 Secret；危险数据库命令要标明目标和回滚；verification 必须附文件、日志、测试或运行证据；不具备外部条件使用 BLOCKED，未执行使用 NOT_RUN。完成后同步当前规范、`09-audit/verification-matrix.md` 和本索引。

## 索引

- `2026/09/flyway-v1-baseline/`：将当前开发阶段数据库完整收口为单一 V1 初始基线，重建 DEV 并完成 Schema/Entity/Flyway/文档核对。

- `2026/09/audit-field-defaults/`：公共字段仅创建时间必填且默认当前时间，删除标识默认 0，同步 Java 和真实 DEV 回归。

- `2026/09/third-round-governance/`：第三轮工程规范、数据库治理、文档和验收体系。
- `2026/09/default-dev-profile/`：修复后端默认未激活 `dev` Profile 的配置问题。
- `2026/09/flyway-v3-repair/`：修复 DEV 数据库 Flyway V3 失败迁移及启动阻断。
- `2026/09/netty-jdk25-warning/`：升级 Netty 并配置 Java 25 开发启动兼容参数。
- `2026/09/app-environments/`：为 uni-app 增加开发/生产环境配置和构建校验。
- `2026/09/app-env-directory/`：统一 app/env 通用、dev、prod 配置目录。
- `2026/09/h5-blank-page/`：补齐平台依赖、恢复官方 Vue 运行时和类型，修复 H5 空白并验证路由。
