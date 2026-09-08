# 02｜数据库规范

本目录是数据库设计和审计的入口。正式 Schema 唯一来源是 `server/src/main/resources/db/migration/`；`sql/` 只允许存放人工审计或运维 SQL，不得用手工脚本替代 Flyway Migration。

## 必须统一的规则

- 主表使用完整 10 个公共审计字段；真正的关联表只使用 `created_at` 和 `deleted`。
- `created_at` 使用 `DATETIME(3)`，必须 `NOT NULL DEFAULT CURRENT_TIMESTAMP(3)`；`deleted` 为 `NULL DEFAULT 0`，0 表示存在、1 表示删除。
- 公共字段注释、业务字段注释和表注释必须明确；正式更新人字段命名为 `update_name`。
- 金额使用整数分或 `BigDecimal`，禁止 `float`/`double`。
- 当前初始化基线是 `V1__init_schema.sql`，结构变化继续通过 V2、V3、V4 等新版本 Migration 演进；不修改共享环境已执行的历史 Migration。

## 当前 Migration 与业务表设计

当前迁移目录包含 `V1__init_schema.sql`、`V2__create_app_file_table.sql`、`V3__create_asset_account_table.sql`、`V4__create_transaction_detail_and_refund_tables.sql` 和 `V5__add_asset_account_sort_order.sql`。V1–V5 是代码仓库中的正式结构定义；某个环境是否已成功执行，必须查询该环境的 `flyway_schema_history` 和 `information_schema`，本目录不把文件存在等同于数据库已落地。

| 版本 | 当前表 | 用途 |
| --- | --- | --- |
| V1 | `app_user`、`user_identity`、`app_login_log` | 用户主数据、第三方身份和登录审计 |
| V2 | `app_file` | MinIO 文件元数据和头像关联 |
| V3 | `asset_account` | 资金账户与信贷账户 |
| V4 | `transaction_detail`、`transaction_refund` | 四类账单和退款记录 |
| V5 | `asset_account.sort_order` | 同类资产账户展示顺序 |

- [`asset-account.md`](asset-account.md)：V3 统一资产账户表，区分资金账户和信贷账户，并说明数据库约束与当前 Service 校验的差异。
- [`transaction-detail-refund.md`](transaction-detail-refund.md)：V4 账单明细主表与独立退款记录表，说明四类账单字段规则、当前实现和退款事务口径。
- [`user-file.md`](user-file.md)：V1 用户/身份/登录审计表与 V2 文件元数据表的当前约束和开放边界。

## 关联说明

表分类和主表/关联表的判断见 [`table-classification.md`](table-classification.md)。新增字段时需要同时核对 Migration、Entity、DTO/VO、TypeScript、测试和迭代档案。
