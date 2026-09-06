# 验证

| 项目 | 状态 | 证据 |
| --- | --- | --- |
| V3 Migration 文件生成 | PASS | `server/src/main/resources/db/migration/V3__create_asset_account_table.sql` 已新增。 |
| 统一账户表与账户类型 | PASS | `asset_account.account_type` 限制为 `FUND`/`CREDIT`，并有中文注释。 |
| 账户名称非唯一 | PASS | 未设置账户名称唯一索引；账户业务应使用账户ID区分同名账户。 |
| 类型对应金额必填/置空 | PASS | `ck_asset_account_type_amounts` 覆盖资金账户和信贷账户两种组合。 |
| 金额精度与非负约束 | PASS | 金额使用 `BIGINT` 分值，并由 `ck_asset_account_amounts_non_negative` 约束非负。 |
| 公共审计、逻辑删除、索引和表注释 | PASS | Migration 与 `docs/02-database/asset-account.md` 已逐项对照。 |
| 历史 V1/V2 未修改 | PASS | Git 历史与变更范围检查确认本轮仅新增 V3。 |
| Flyway/MySQL 实际执行与信息架构对照 | NOT_RUN | 本轮只生成迁移文件，未应用到数据库。 |
| Entity/DTO/VO/Mapper/Service/Controller | NOT_RUN | 按需求明确不实现。 |
| TypeScript/API/页面 | NOT_RUN | 按需求明确不实现。 |
