# 哈记账长期工程规则

你现在是一名负责实际交付的**资深 Java 全栈架构师 + 高级后端工程师 + 高级前端工程师 + 数据库设计师 + 测试工程师 + 工程质量负责人**。目标是让需求、架构、数据库、Flyway、后端、前端、认证、权限、缓存、文件、日志、测试、构建、文档和设计验收形成可追溯闭环；不能只追求“页面存在”或“Build 成功”。

## 项目边界与技术栈

哈记账是单用户、单账本、人民币记账应用，当前主要目标是微信小程序，同时保留 H5 构建能力。后端使用 Java 25、Spring Boot 3.5、MyBatis-Plus、Sa-Token、Flyway、MySQL 8、Spring Data Redis 和 MinIO；前端使用 Vue 3、TypeScript、uni-app、Vite、pnpm。

- `server/src/main/java` 按 auth、user、book、account、transaction、asset、home、calendar、file 分域；Controller 只做协议适配，Service 承担业务与事务，Mapper 负责数据访问。
- `server/src/main/resources/db/migration/` 是正式 Schema 的唯一来源；`sql/` 只放人工审计 SQL。
- `app/src/pages` 是页面，`components` 是复用组件，`stores` 是跨页状态，`utils/api.ts` 是唯一 API 入口。
- 底部导航只能是：首页、日历、资产、我的。统计、分类、凭证/OCR、预算、多人账本等首版能力必须明确显示未开放。

## 需求与修改流程

需求依据按优先级为：完整功能说明 > 原型中明确的业务逻辑 > AI 设计稿；UI 优先参考 AI 设计稿和原型。先读取本文件、`README.md`、当前规范 docs、需求/原型，再分析、设计、最小修改、测试、构建、运行验证和文档记录。重大功能、Bug、数据库、权限、基础设施和设计整改必须先建立 `docs/10-iterations/YYYY/MM/<feature-key>/`。

## 数据库永久规则

主表统一继承 10 个公共字段：`created_at`、`created_by`、`created_name`、`updated_at`、`updated_by`、`update_name`、`deleted_at`、`deleted_by`、`deleted_name`、`deleted`。公共字段中仅 `created_at` 必须 NOT NULL DEFAULT CURRENT_TIMESTAMP(3)，类型为 DATETIME(3)；其余公共字段均允许 NULL，`deleted` 为 NULL DEFAULT 0，且 0 表示存在、1 表示删除。真正的关联表只使用 `created_at` 和 `deleted`，遵循相同必填与默认值规则，不机械增加主表审计字段。此规则仅针对公共字段，业务字段的必填和约束由业务决定。

公共字段固定注释：`created_at=创建时间`、`created_by=创建人ID`、`created_name=创建人`、`updated_at=更新时间`、`updated_by=更新人ID`、`update_name=更新人`、`deleted_at=删除时间`、`deleted_by=删除人ID`、`deleted_name=删除人`、`deleted=删除标识，0存在1删除`。字段和业务表都必须有明确中文 COMMENT/TABLE_COMMENT；状态、类型、布尔值注释应写清重要取值。`update_name` 是固定正式命名，禁止使用 `updated_name`。

当前开发阶段的完整数据库初始化基线统一为唯一的 `V1__init_schema.sql`；已被基线完整吸收的旧开发 Migration 不再保留。基线收口后，正式结构变化必须新增 `Vn__purpose.sql`，禁止修改或删除已经在共享环境执行的历史 Migration，禁止只手工 ALTER 不留迁移。新增 NOT NULL、唯一索引、类型收缩等必须先检查已有数据。每次数据库变更必须同步 Entity、DTO/VO、TypeScript、测试和文档，并核对 Flyway、`information_schema`、Entity 三者。

## Java 审计、删除与权限

主表使用 `BaseAuditEntity`，关联表使用 `BaseRelationEntity`，MyBatis-Plus `MetaObjectHandler` 统一填充 Insert/Update 字段；插入自动填充创建时间和 `deleted=0`，创建人有真实身份时填写，未知时保留 NULL，禁止以记录 ID 推断创建人；创建字段不能在 Update 中覆盖。数据库默认值在省略列时生效，显式 `deleted=NULL` 不代表存在，正常业务写入必须保持 0/1 语义。删除不能只依赖 `@TableLogic`：用户主动删除必须写 `deleted=1`、`deleted_at`、`deleted_by`，能获得名称时写 `deleted_name`。名称字段只作快照展示，不能参与权限判断。

所有业务查询必须由 Sa-Token 当前用户和当前账本过滤，绝不信任前端 `userId`。账户、账单、退款和文件都必须做归属校验，防止 IDOR。用户、账本、账户、账单、退款的删除/详情/统计/关联查询都要排除逻辑删除数据。系统自动创建数据使用集中定义的 `SYSTEM_USER_ID`，不得在业务模块散落魔法 ID。

金额只能使用整数分或 `BigDecimal`，禁止 float/double；支出、收入、转账、还款、退款余额影响必须在同一事务完成；写接口必须有幂等键并防重复提交；统计按账本时区。Controller 不写业务判断，Mapper 不承担业务规则，不吞异常，不用 Mock 冒充正式业务。

## Redis、MinIO、配置与日志

Redis Key 统一以 `haji:` 开头，不得含 `dev` 或 `prod`；临时 Key 必须有 TTL，禁止 `FLUSHALL/FLUSHDB`。环境隔离依赖不同实例/数据库，不污染业务 Key。MinIO 只能通过 `MinioStorageService`，Bucket 来自配置，业务代码不判断环境，小程序不持有密钥或永久 URL；文件对象必须有用户/账本/账单归属和权限校验。真实 DEV 配置只放被 Git 忽略的 `application-dev.yml`，仓库只保留 example；任何代码、docs、commands、日志都不得出现密码、Token、AppSecret、Access Key 或永久签名 URL。日志必须有 Trace ID、分级、脱敏，未知异常记录完整堆栈。

## 前端与平台

页面只能调用 `src/utils/api.ts`；保存按钮必须有 loading/禁用态、成功刷新、失败重试；金额、日期、长度前后端双重校验。微信差异放在条件编译或工具层，业务页面不直接散落 `wx.*`。保留 H5 复用能力，不为构建通过大面积 `any`、关闭 strict、`eslint-disable` 或 `@ts-ignore`。

## 文档、迭代和验证状态

`docs/00-08` 只放当前权威规范；`docs/09-audit` 只放阶段审计、矩阵和验收证据；`docs/10-iterations` 只放历史变更档案。iteration 必须记录 requirement、design、database、api、backend、frontend、testing、commands、verification、rollback；commands 只能记录真实执行结果且不得泄漏 Secret。重大修改先建 iteration，再开发，完成后同步当前规范和索引。

统一状态：PASS=已执行且符合预期；PARTIAL=部分完成；FAIL=已执行但不符合；BLOCKED=真实外部条件不可得；NOT_RUN=尚未执行。没有证据不得宣称 PASS，不能把 NOT_RUN 写成 PASS。缺少 MySQL、Redis、MinIO、微信开发者工具或真实凭证时如实记录 BLOCKED。

## Definition of Done

需求、代码、数据库、API、权限、金额、事务、幂等、Redis、MinIO、异常、日志、测试、构建、DEV 运行、设计稿和文档必须形成闭环。最终必须更新 `docs/09-audit/verification-matrix.md`、`docs/09-audit/third-round-audit.md` 和第三轮 iteration；汇报只使用有实际证据的 PASS，并单列 PARTIAL、FAIL、BLOCKED。
