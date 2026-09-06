# 数据库规范

正式 Schema 唯一来源是 `server/src/main/resources/db/migration/`。当前开发阶段完整数据库初始化基线为唯一的 `V1__init_schema.sql`，已被基线吸收的旧 V2/V3/V4 不再保留。基线收口后，后续结构变化必须新增版本迁移。当前公共字段规则直接由 V1 定义：主表使用 10 个审计字段，关联表使用 `created_at + deleted`，并保留 `user_identity.updated_at` 历史列；逻辑删除统一 `0=存在、1=删除`。所有业务表和字段必须有明确中文 COMMENT。

| 公共字段 | 数据库约束 | 应用规则 |
| --- | --- | --- |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | 插入自动填充，数据库支持省略列；更新不覆盖 |
| `deleted` | `TINYINT NULL DEFAULT 0` | 正常插入自动填充 0，删除写 1 |
| 其余八个主表公共字段 | 允许 NULL，默认 NULL | 有真实操作信息时填写；创建人未知时保留空值 |

只有公共字段中的 `created_at` 必填；业务主键、归属、金额等仍按各自业务约束执行。数据库默认值仅在省略列或使用 DEFAULT 时生效，显式 `deleted=NULL` 不自动变成 0，也不视为存在；正常应用填充 0，查询保留 `deleted=0` 语义。系统任务可显式使用集中定义的 `SYSTEM_USER_ID`，不能把任意未知身份自动当作系统或记录 ID。

关联表 `user_identity` 保留历史 `updated_at` 列并改为可空，Entity 仍仅使用两个公共字段。V1 不回填业务数据。DEV 可通过 `mvn test "-Dledger.audit.dev=true"` 显式执行迁移和真实库回归；该测试只允许 `haji_dev`，测试业务数据事务回滚，普通 `mvn test` 跳过此项。开发数据库重置使用显式的 `-Dledger.dev.reset=true`，由 Flyway clean 后再从空库 migrate V1，禁止手工伪造历史。

`table-classification.md` 是表分类清单；`../../sql/third-round-schema-audit.sql` 用于人工 information_schema 审计。
