# 资产账户表设计

## 1. 表用途

`asset_account` 是资产页资金账户和信贷账户的统一主表。账户是可独立新增、编辑、停用和查询的业务对象，因此使用完整的 10 个公共审计字段，而不是关联表字段模型。

本表只保存账户当前展示所需的账户属性：资金账户的余额、信贷账户的总额度与当前欠款，以及是否计入净资产。账户明细、交易记录、还款记录和统计不在本次表设计范围内。

净资产口径为：计入净资产的资金账户余额之和，减去计入净资产的信贷账户当前欠款之和。信贷账户的当前欠款以正数表示负债，不保存负数欠款。

## 2. 统一表方案

采用一张表，通过 `account_type` 区分：

| `account_type` | 中文含义 | 必填金额字段 | 必须为空的金额字段 |
| --- | --- | --- | --- |
| `FUND` | 资金账户 | `balance_cent` | `total_limit_cent`、`current_debt_cent` |
| `CREDIT` | 信贷账户 | `total_limit_cent`、`current_debt_cent` | `balance_cent` |

统一表可以让资产页按同一用户、删除和净资产标识查询，也方便后续交易表通过一个账户ID关联。金额列虽然在物理上共存，但通过 `ck_asset_account_type_amounts` 强制维护类型与列的对应关系，因此当前范围内不会造成可解释性或约束维护问题。只有当两类账户出现大量独有字段、不同生命周期或不同授权模型时，才应评估拆分为 `fund_account` 与 `credit_account`。

## 3. 字段定义

### 3.1 业务字段

| 字段 | 类型 | 默认值 | 非空 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `BIGINT` | 无 | 是 | 账户ID，由应用生成。 |
| `user_id` | `BIGINT` | 无 | 是 | 所属用户ID，数据库外键关联 `app_user.id`；业务查询仍必须校验当前 Sa-Token 用户。 |
| `account_name` | `VARCHAR(64)` | 无 | 是 | 账户名称；`CHECK` 限制去除首尾空白后为1至20个字符，允许重复。 |
| `account_type` | `VARCHAR(16)` | 无 | 是 | 账户类型：`FUND`资金账户、`CREDIT`信贷账户；不设置默认值，创建时必须显式选择类型。 |
| `total_limit_cent` | `BIGINT` | `NULL` | 否 | 信贷账户总额度，单位为分，非负；资金账户必须为 `NULL`。 |
| `current_debt_cent` | `BIGINT` | `NULL` | 否 | 信贷账户当前欠款，单位为分，非负且不得大于总额度；资金账户必须为 `NULL`。 |
| `balance_cent` | `BIGINT` | `NULL` | 否 | 资金账户余额，单位为分，非负；信贷账户必须为 `NULL`。 |
| `include_net_asset` | `TINYINT` | `1` | 是 | 净资产标识：`0`不计入，`1`计入。 |

金额使用整数分，避免 `float`/`double` 的精度问题。当前金额字段使用有符号 `BIGINT` 并配合非负 `CHECK`；后续如果需要明确业务上限，应在业务规则和接口校验中同步限定，不在本次 Schema 中人为收窄到某个产品上限。

### 3.2 公共审计字段

表中包含 `created_at`、`created_by`、`created_name`、`updated_at`、`updated_by`、`update_name`、`deleted_at`、`deleted_by`、`deleted_name`、`deleted`。其中 `created_at` 为 `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)`；其余审计字段允许 `NULL`，`deleted` 为 `NULL DEFAULT 0`，并遵循 `0`存在、`1`删除语义。主动删除时应写入 `deleted=1`、删除时间和删除人信息，不能仅依赖 `@TableLogic`。

## 4. 约束和索引

- `PRIMARY KEY (id)`：保证账户ID唯一。
- `idx_asset_account_user_type_deleted (user_id, account_type, deleted)`：支持按当前用户、账户类型和有效状态查询。
- `fk_asset_account_user`：保证所属用户存在；当前单账本模型不在账户表重复保存账本字段。
- `ck_asset_account_type`、`ck_asset_account_include_net_asset`、`ck_asset_account_deleted`：限制类型和标识取值。
- `ck_asset_account_amounts_non_negative`：金额均不得为负数。
- `ck_asset_account_type_amounts`：强制账户类型与金额列的必填/置空关系，并限制信贷账户当前欠款不超过总额度。

所有业务查询仍须同时使用当前用户和 `deleted=0` 条件；账户名称允许重复，业务展示和账户选择必须使用账户ID区分，名称快照只用于展示，不能参与权限判断。

## 5. Flyway 变更

- 文件：`server/src/main/resources/db/migration/V3__create_asset_account_table.sql`
- 变更：新增 `asset_account` 表。
- 未修改 `V1__init_schema.sql` 或 `V2__create_app_file_table.sql`。
- 当前工作区未执行 V3；需要在可用 MySQL 环境中通过应用/Flyway 执行后，再用 `flyway_schema_history`、`information_schema` 和后续 Entity 对照验收。

## 6. 后续扩展建议

1. 若未来从单账本扩展为多账本，应新增账本归属模型并评估为账户表引入账本归属字段，同时核对用户与账本的归属一致性。
2. 账户业务落地时增加幂等键、防重复提交、当前用户校验和主动软删除逻辑。
3. 交易表落地后，余额和当前欠款应由同一事务中的交易/还款规则维护；不要在账户接口中绕过流水直接改余额，除非另有经过审计的余额调整模型。
4. 若支持多币种、分期、利息、手续费或投资账户，应先扩展领域模型和金额口径，再评估是否拆表；本表当前只服务人民币单账本场景。
