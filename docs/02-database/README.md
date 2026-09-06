# 02｜数据库规范

本目录是数据库设计和审计的入口。正式 Schema 唯一来源是 `server/src/main/resources/db/migration/`；`sql/` 只允许存放人工审计或运维 SQL，不得用手工脚本替代 Flyway Migration。

## 必须统一的规则

- 主表使用完整 10 个公共审计字段；真正的关联表只使用 `created_at` 和 `deleted`。
- `created_at` 使用 `DATETIME(3)`，必须 `NOT NULL DEFAULT CURRENT_TIMESTAMP(3)`；`deleted` 为 `NULL DEFAULT 0`，0 表示存在、1 表示删除。
- 公共字段注释、业务字段注释和表注释必须明确；正式更新人字段命名为 `update_name`。
- 金额使用整数分或 `BigDecimal`，禁止 `float`/`double`。
- 当前开发基线为唯一 `V1__init_schema.sql`；后续结构变化新增版本，不修改共享环境已执行的历史 Migration。

## 当前业务表设计

- [`asset-account.md`](asset-account.md)：V3 统一资产账户表，区分资金账户和信贷账户。

## 关联说明

表分类和主表/关联表的判断见 [`table-classification.md`](table-classification.md)。新增字段时需要同时核对 Migration、Entity、DTO/VO、TypeScript、测试和迭代档案。
