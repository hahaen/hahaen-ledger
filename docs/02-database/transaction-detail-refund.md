# 账单明细与退款数据库设计

更新日期：2026-09-06。

本设计依据 `AGENTS.md`、`README.md`、`哈记账微信小程序_完整功能说明.md`、`哈记账微信小程序_AI设计稿需求.md`、`哈记账小程序_原型设计稿/README.md` 及原型 `app.js` 的账单、还款和退款交互制定。正式结构以 `server/src/main/resources/db/migration/V4__create_transaction_detail_and_refund_tables.sql` 为准。

## 1. 范围与关键决策

- 新增 `transaction_detail` 账单明细主表，使用 `transaction_type` 区分 `EXPENSE`、`INCOME`、`TRANSFER`、`REPAYMENT`。
- 新增 `transaction_refund` 退款记录关联表。退款不是账单类型，不使用父子账单、自关联或退款类型字段。
- 金额使用整数分，字段名按本次需求保留为 `original_amount`、`amount`、`refund_amount`；三者均禁止按元保存浮点值。
- 主表和退款表均使用完整 10 个公共审计字段。退款虽通过 `transaction_id` 关联账单，但有独立退款编号、金额、幂等键和独立逻辑删除生命周期，不属于只表达关系的纯关联表；完整审计字段用于满足退款删除可追溯要求。
- 当前数据库没有 `app_book` 表，且 `asset_account` 按单用户、单账本模型仅保存 `user_id`。因此 `transaction_detail` 不重复保存 `book_id`，通过 `user_id` 归属当前用户及其唯一账本。当前用户、当前账本上下文、账户类型、账户归属和逻辑删除由 Service 层校验；未来扩展多账本时再单独设计账本归属模型。

## 2. 需求与原型字段映射

| 需求/原型概念 | 数据库字段 | 说明 |
| --- | --- | --- |
| 每条账单唯一记录编号 | `transaction_detail.transaction_no` | 独立于主键的用户可读编号，唯一索引保证不重复。 |
| 账单主键 | `transaction_detail.id` | 内部关联 ID；也是退款表的 `transaction_id` 外键目标。 |
| 所属用户及当前账本 | `user_id` | 当前单账本模型不在账单表重复保存 `book_id`；`user_id` 外键关联 `app_user.id`。 |
| 支出 | `transaction_type=EXPENSE`、`account_id` | `account_id` 必须是有效资金账户；原型的金额、账户、日期时间、备注分别映射金额字段、账户字段、`occurred_at`、`note`。 |
| 收入 | `transaction_type=INCOME`、`account_id` | 结构同支出，金额方向由 Service 按收入规则处理。 |
| 转账 | `transaction_type=TRANSFER`、`from_account_id`、`to_account_id` | 转出和转入账户均必填且不能相同，均须为资金账户。 |
| 还款 | `transaction_type=REPAYMENT`、`from_account_id`、`to_account_id` | `from_account_id` 为还款资金账户，`to_account_id` 为还款信贷账户。 |
| 记账日期与时间 | `occurred_at` | `DATETIME(3)`，保存业务发生时间；`created_at` 保存服务端创建时间。 |
| 备注 | `note` | `VARCHAR(100)`，与原型 100 字限制一致。 |
| 退款按钮/退款记录 | `transaction_refund` | 仅 Service 对支出、收入开放；每笔退款独立一行。 |
| 退款时间 | `transaction_refund.created_at` | 系统生成的退款记录创建时间，不另设用户输入的 `refunded_at`。 |
| 退款编号 | `transaction_refund.refund_no` | 独立退款编号，唯一索引保证不重复。 |
| 是否退款 | `transaction_detail.has_refund` | `0` 未发生过退款，`1` 已发生过退款；退款删除后仍保留历史发生标识。 |

原型中出现的分类、凭证、拆分账单、手续费等不是本次字段；不在 V4 中增加。

## 3. `transaction_detail` 主表

### 3.1 字段设计

| 字段 | 类型 | 空值/默认 | 适用范围与约束 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | NOT NULL | 主键，内部账单明细 ID。 |
| `user_id` | `BIGINT` | NOT NULL | 所属用户，外键关联 `app_user.id`；Service 必须使用当前会话用户。 |
| `transaction_no` | `VARCHAR(64)` | NOT NULL | 唯一账单记录编号，不能为空白。 |
| `transaction_type` | `VARCHAR(16)` | NOT NULL | 只允许 `EXPENSE`、`INCOME`、`TRANSFER`、`REPAYMENT`。退款不占用取值。 |
| `original_amount` | `BIGINT` | NOT NULL | 原始金额，单位为分；大于 0，最大对应 ¥999,999,999.99。 |
| `amount` | `BIGINT` | NOT NULL | 当前有效金额，单位为分；`0 <= amount <= original_amount`。 |
| `has_refund` | `TINYINT` | NOT NULL DEFAULT 0 | 只允许 0/1；0 时要求 `amount=original_amount`，1 表示曾有退款。 |
| `account_id` | `BIGINT` | NULL | 仅支出/收入使用，必须为资金账户；其他类型必须为空。 |
| `from_account_id` | `BIGINT` | NULL | 仅转账/还款使用，表示资金流出账户。 |
| `to_account_id` | `BIGINT` | NULL | 仅转账/还款使用，表示资金流入或还款目标账户。 |
| `occurred_at` | `DATETIME(3)` | NOT NULL | 用户选择的业务记账日期与时间。 |
| `note` | `VARCHAR(100)` | NULL | 备注，最多 100 个字符。 |
| `idempotency_key` | `VARCHAR(80)` | NULL | 写接口幂等键；同一用户内唯一，空值允许多行。 |
| `created_at` | `DATETIME(3)` | NOT NULL DEFAULT CURRENT_TIMESTAMP(3) | 创建时间。 |
| `created_by` / `updated_by` / `deleted_by` | `BIGINT` | NULL | 创建、更新、删除人 ID。 |
| `created_name` / `update_name` / `deleted_name` | `VARCHAR(100)` | NULL | 创建、更新、删除人名称快照；不参与权限判断。 |
| `updated_at` / `deleted_at` | `DATETIME(3)` | NULL | 更新时间、删除时间。 |
| `deleted` | `TINYINT` | NULL DEFAULT 0 | 0 存在、1 删除；业务查询使用 `deleted=0`。 |

### 3.2 四类账单字段使用规则

| 类型 | `account_id` | `from_account_id` | `to_account_id` | 数据库可强制的结构规则 | Service 必须补充的规则 |
| --- | --- | --- | --- | --- | --- |
| `EXPENSE` 支出 | 必填，资金账户 | NULL | NULL | 类型与字段组合 | 账户属于当前用户/账本且未删除；余额影响与幂等。 |
| `INCOME` 收入 | 必填，资金账户 | NULL | NULL | 类型与字段组合 | 账户属于当前用户/账本且未删除；余额影响与幂等。 |
| `TRANSFER` 转账 | NULL | 必填，资金账户 | 必填，资金账户 | 两账户不能相同 | 两账户归属当前用户/账本且未删除；不进入收支统计。 |
| `REPAYMENT` 还款 | NULL | 必填，资金账户 | 必填，信贷账户 | 两账户不能相同 | 来源余额充足、目标欠款足够、账户归属有效；不允许退款。 |

V4 的外键只能保证账户 ID 对应某条 `asset_account` 记录，不能表达账户类型、用户/当前账本一致性或 `deleted=0`；这些必须由 Service 在创建、编辑、删除、详情和统计查询中校验。

## 4. 金额字段与退款语义

- `original_amount` 是初始入账金额，创建账单时写入正整数分。正常编辑不得以退款后的 `amount` 覆盖它；若允许编辑原始金额，Service 必须先校验新原始金额不小于有效退款累计，并在同一事务内重算。
- `amount` 是当前有效金额，定义为 `original_amount - 有效退款累计金额`。支出、收入统计和账单展示使用它；转账、还款不做退款。
- `has_refund=0` 表示从未发生退款；`has_refund=1` 表示至少成功创建过一笔退款。它是历史提示字段，不用于替代退款明细，也不单独推导当前状态。当前“完成/部分退款/已退款”应依据 `amount` 与 `original_amount` 及有效退款记录判断。
- 金额上限按原型的 ¥999,999,999.99 落库为 99,999,999,999 分；数据库约束负责非负、上下界和当前金额不超过原始金额，Service 负责元转分、两位小数和错误提示。

## 5. `transaction_refund` 退款表

### 5.1 设计

| 字段 | 类型 | 空值/默认 | 说明 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | NOT NULL | 退款记录主键。 |
| `transaction_id` | `BIGINT` | NOT NULL | 原始账单明细 ID，外键关联 `transaction_detail.id`。不保存账单类型。 |
| `refund_no` | `VARCHAR(64)` | NOT NULL | 唯一退款编号，不能为空白。 |
| `refund_amount` | `BIGINT` | NOT NULL | 本次退款金额，单位为分，必须大于 0；一行只保存一次退款金额。 |
| `idempotency_key` | `VARCHAR(80)` | NULL | 同一原始账单内唯一的退款写操作幂等键。 |
| `created_at` | `DATETIME(3)` | NOT NULL DEFAULT CURRENT_TIMESTAMP(3) | 系统生成的退款时间，也是创建时间。 |
| `created_by` / `updated_by` / `deleted_by` | `BIGINT` | NULL | 创建、更新、删除人 ID。 |
| `created_name` / `update_name` / `deleted_name` | `VARCHAR(100)` | NULL | 创建、更新、删除人名称快照；不参与权限判断。 |
| `updated_at` / `deleted_at` | `DATETIME(3)` | NULL | 更新时间、删除时间。 |
| `deleted` | `TINYINT` | NULL DEFAULT 0 | 0 存在、1 逻辑删除；退款累计只统计 `deleted=0`。 |

退款表是带业务生命周期的关联记录表，不是只表达多表关系的纯关联表，因此采用完整主表审计字段。退款删除必须通过逻辑删除实现并填充删除审计字段；主表外键默认限制物理删除，避免留下无主退款记录。

### 5.2 关系与索引

```text
app_user (1) ──< transaction_detail (1) ──< transaction_refund (N)
asset_account (1) ──< transaction_detail
```

- 主表唯一索引：`uk_transaction_detail_transaction_no`、`uk_transaction_detail_user_idempotency`。
- 主表查询索引：按用户/日期、用户/类型/日期，以及单账户、转出账户、转入账户分别建立索引。
- 退款表唯一索引：`uk_transaction_refund_refund_no`、`uk_transaction_refund_transaction_idempotency`。
- 退款累计/记录列表索引：`idx_transaction_refund_transaction_created (transaction_id, deleted, created_at)`。
- 当前单账本实现通过 `user_id` 范围查询账单；所有查询仍必须带当前用户和 `deleted=0`，不能信任前端伪造的用户上下文。

## 6. 多笔退款保存与事务规则

创建退款时，Service 必须在一个数据库事务中完成以下顺序（具体锁顺序可按实现调整，但必须锁住原账单）：

1. 按当前用户查询有效原账单，并使用 `SELECT ... FOR UPDATE` 锁定 `transaction_detail`；原账单必须存在、`deleted=0`，类型只能是 `EXPENSE` 或 `INCOME`。
2. 校验本次退款金额为正整数分，且有效退款累计加本次金额不大于 `original_amount`。有效累计只统计同一 `transaction_id` 且 `deleted=0` 的退款记录。
3. 生成唯一 `refund_no`，按幂等键识别重复请求，插入一行 `transaction_refund`。
4. 更新同一账单的 `amount = original_amount - 有效退款累计（含本次）`，并设置 `has_refund=1`，同时写入更新审计字段。
5. 任一步失败都回滚退款记录和账单金额更新；成功后再提交。

删除退款时也必须在同一事务中锁定原账单：先将退款记录写为 `deleted=1`，再按未删除退款记录重算 `amount`。即使最后一笔退款被删除，`has_refund` 仍保持 1 以保留“曾发生过退款”的事实；若产品最终要表达“当前有有效退款”，应另用派生状态，不能改变本字段历史语义。

并发退款必须以父账单行锁或等价的串行化方案保护，不能只先 `SUM` 再插入。直接手工修改 `amount`、插入退款记录而不更新父账单，均属于数据不一致操作。

## 7. 数据库约束与 Service 约束边界

### 数据库保证

- 主键、`transaction_no`、`refund_no` 唯一性。
- 用户、账户、原始账单外键存在性；单账本不在账单表重复保存账本外键。
- 账单类型枚举、金额为整数分且范围合法、`amount <= original_amount`。
- 类型与账户字段的必填/置空关系，转账/还款转出与转入账户不能相同。
- `has_refund`、`deleted` 取值范围及空白编号/幂等键检查。
- 所有表/字段中文注释和 InnoDB、utf8mb4 存储属性。

### Service 保证

- Sa-Token 当前用户和当前账本上下文，禁止信任前端 `userId` 或任何未由服务端解析的归属信息作为权限依据。
- 账户类型、账户未删除、账户和账单属于同一用户；已停用账户只能保留历史关联。
- 只有支出/收入可退款；转账/还款拒绝退款。
- 多笔退款累计不超过原始金额；逻辑删除账单/退款不参与计算。
- 创建退款、更新 `amount`、更新 `has_refund` 的同事务性、父行锁、幂等和重复提交防护。
- 账户余额、信贷欠款、统计口径与账单/退款金额同步更新；编辑/删除退款恢复相应有效金额。
- 主动删除填充删除时间、删除人和可获得的删除人名称；名称快照不参与权限。

## 8. Flyway V4 变更说明

- 新增文件：`server/src/main/resources/db/migration/V4__create_transaction_detail_and_refund_tables.sql`。
- 新增表：`transaction_detail`、`transaction_refund`。
- 未修改或删除 V1、V2、V3；未修改 Java Entity、DTO、VO、Mapper、Service、Controller、前端页面或 TypeScript。
- V4 先建主表，再建退款表，保证退款外键目标已存在；未加入危险的自动回滚 SQL。
- 未修改 V2 的 `app_file` 外键。现有 `app_file.book_id` 是文件服务历史兼容字段，`app_file.transaction_id` 已使用统一命名，后续可单独补充关联约束。

## 9. 验证、状态与回滚

本次交付阶段可执行的静态验证命令和结果记录在 `docs/10-iterations/2026/09/transaction-detail-refund-schema/08-commands.md`、`09-verification.md`。若本机没有可连接的 MySQL，Flyway 实际执行、`information_schema` 对照、约束运行时行为必须标记 `BLOCKED` 或 `NOT_RUN`，不得写成 PASS。

推荐在安全的开发数据库执行：

```powershell
cd server
mvn test
mvn spring-boot:run
```

应用启动后核对 `flyway_schema_history`、`information_schema.tables`、`information_schema.columns`、`information_schema.statistics`、`information_schema.table_constraints`，并用隔离测试数据验证多笔退款、超额退款、逻辑删除退款、转账/还款拒绝退款和并发串行化。不要在正式 Migration 中加入 `DROP TABLE` 或自动回滚语句。

若 V4 尚未执行，回滚仅需删除未部署的 V4 文件并重新生成校验；若已在共享环境执行，不修改或删除 V4，应通过评估数据后的新版本 Migration 实施可审计的反向变更。任何物理删除前必须确认没有业务数据和外键依赖，默认优先保留数据并逻辑停用。
