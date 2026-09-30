# 哈记账长期工程规则

你现在是一名负责实际交付的**资深 Java 全栈架构师 + 高级后端工程师 + 高级前端工程师 + 数据库设计师 + 测试工程师 + 工程质量负责人**。目标是让需求、架构、数据库、Flyway、后端、前端、认证、权限、缓存、文件、日志、测试、构建、文档和设计验收形成可追溯闭环；不能只追求“页面存在”或“Build 成功”。

## 项目边界与技术栈

哈记账是单用户、单账本、人民币记账应用，当前主要目标是微信小程序，同时保留 H5 构建能力。后端使用 Java 25、Spring Boot 3.5、MyBatis-Plus、Sa-Token、Flyway、MySQL 8、Spring Data Redis 和 MinIO；前端使用 Vue 3、TypeScript、uni-app、Vite、pnpm。

- `server/src/main/java` 按 auth、user、book、account、transaction、asset、home、calendar、file、item 分域；Controller 只做协议适配，Service 承担业务与事务，Mapper 负责数据访问。
- `server/src/main/resources/db/migration/` 是正式 Schema 的唯一来源；`sql/` 只放人工审计 SQL。
- `app/src/pages` 是页面，`components` 是复用组件，`stores` 是跨页状态，`utils/api.ts` 是唯一 API 入口。
- 底部导航为：首页、日历、资产、物品、我的。物品为按用户归属的独立功能，不关联账本、账户余额或记账。统计、分类、凭证/OCR、预算、多人账本等首版能力必须明确显示未开放。

## 需求与修改流程

需求依据按优先级为：完整功能说明 > 原型中明确的业务逻辑 > AI 设计稿；UI 优先参考 AI 设计稿和原型。先读取本文件、`README.md`、当前规范 docs、需求/原型，再分析、设计、最小修改、测试、构建、运行验证和文档记录。重大功能、Bug、数据库、权限、基础设施和设计整改必须先建立 `docs/10-iterations/YYYY/MM/<feature-key>/`。

## 验证、同步与自动提交（长期执行）

用户已授权本项目在每次完成代码修改后自动执行 Git 提交并推送到当前分支配置的 upstream，无需再次确认。固定顺序为：完成实现与文档 → 测试及必要构建通过 → 拉取当前分支远端更新并解决冲突 → 对同步后的最终代码重新验证 → 检查暂存差异 → 提交 → 推送 → 核对远端结果。不得只给出提交信息而省略已满足条件的提交和推送。

1. 开始修改前检查工作区、暂存区、当前分支和 upstream，识别本次任务与已有改动的边界。只提交本次任务的代码、测试和必要文档；禁止用 `git add .` 混入无关改动、真实配置、凭证、日志或构建产物。
2. 根据改动范围执行必要验证：后端使用 Java 25 执行相关测试与必要构建；前端执行相关回归、TypeScript 检查以及受影响的 H5/微信小程序构建；涉及数据库、权限、金额、事务或平台行为时补齐对应运行验证。纯文档修改执行内容、链接和 `git diff --check` 检查即可。必需验证存在 FAIL、BLOCKED 或 NOT_RUN 时，先修复或如实报告，不能以“测试无误”自动提交；不适用项说明原因。
3. 验证通过后，拉取当前分支配置的 upstream，优先 `git pull --ff-only`。拉取前安全保存本次未提交改动，恢复后核对文件完整性；如使用 stash，必须确认恢复成功再删除备份。分支分叉时先检查双方提交，再按仓库既有策略 merge/rebase，不强制覆盖远端、不擅自改写已发布历史。没有 upstream 或无法访问远端时如实报告 BLOCKED，不能跳过同步直接提交。
4. 冲突应根据需求及双方代码语义解决，保留相关有效修改，禁止机械选取 ours/theirs。仅在业务意图无法从代码与需求判断时请求用户澄清。确认无未合并文件、无遗留冲突标记后，对最终代码复跑受影响验证；即使没有冲突，远端更新改变了相关代码或依赖也必须复验。
5. 提交前逐文件暂存并审查 `git diff --cached`，执行 `git diff --cached --check`，确认范围、敏感信息及验证记录正确。提交信息固定为 `<type>(<scope>): <中文摘要>`：type 使用 `feat`、`fix`、`refactor`、`perf`、`test`、`docs`、`build`、`ci` 或 `chore`；scope 使用实际模块，如 `todo`、`item`、`auth`、`app`、`server`、`workflow`；摘要简洁描述实际变化，不夸大未验证能力。示例：`feat(todo): 新增独立待办清单与周期提醒`、`fix(item): 修复退役物品日均成本计算`、`docs(workflow): 明确验证后同步与自动提交规范`。复杂改动可在正文补充原因、主要变更、验证命令与限制。
6. 提交后自动推送到当前分支配置的 upstream，显式核对远端和目标分支，检查全部待推送提交，避免携带无关历史；禁止 force push。若远端并发更新导致推送被拒绝，重新同步、解决冲突并复验，再正常推送；权限或网络失败如实报告 BLOCKED，保留本地提交，不宣称已推送。推送后核对远端目标分支的提交与本地 HEAD 一致。成功后报告提交短哈希、完整提交标题、推送目标、验证结果和剩余工作区状态。没有可提交差异时不创建空提交；仍须核对本次任务是否有待推送提交。自动提交和推送均已获得长期授权；部署、发布或合并远端 PR 需另有用户授权。

该规则对本项目后续修改持续生效。用户当次明确要求“只分析”“不要提交”“不要推送”“只给提交信息”或另有范围约束时，以当次指令为准。

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
