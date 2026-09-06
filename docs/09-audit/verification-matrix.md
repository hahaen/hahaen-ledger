# 第三轮验证矩阵

状态：PASS=已执行且符合预期；PARTIAL=部分完成；FAIL=已执行但不符合；BLOCKED=真实外部条件不可得；NOT_RUN=尚未执行。

| 范围 | 事实来源/入口 | 证据 | 状态 |
| --- | --- | --- | --- |
| H5 登录/注册原型 | 根目录原型、两份产品文档、h5-auth-prototype 迭代 | 已补充登录/注册页、图形验证码展示/点击图片刷新、验证码错误、失败/禁用态和路由说明；本机浏览器核心交互复验通过，精确视口复验未执行 | PARTIAL |
| H5/微信登录差异 | 原型认证预览、完整功能说明、AI 设计稿需求 | H5 默认账号登录；微信小程序自动登录直达首页；真实 H5 接口尚未实现 | PARTIAL |
| 需求与原型 | 根目录三份产品资料 | 静态复核 | PASS |
| 数据库分类/注释 | V1、table-classification、information_schema | DEV clean 后从 V1 重建；6/6 表注释、全部字段 COMMENT、主表审计字段 50/50、删除标识 6/6 核对 | PASS |
| 公共字段可空/默认值（当前 V1） | V1、AuditFieldDefaultsDevTest、BaseAuditEntity/BaseRelationEntity | haji_dev 从空库执行 V1 并 validate；53 列可空/默认/注释、六表默认插入/NULL 与事务回滚通过 | PASS |
| Java Entity/自动填充 | BaseAuditEntity、BaseRelationEntity、MetaObjectHandler | `mvn test`、`mvn clean package` | PASS |
| API/权限/IDOR | Service 当前用户和账本条件 | 单元/HTTP 回归待执行 | NOT_RUN |
| 逻辑删除/统计 | `deleted` + `@TableLogic` + Service | 真实数据库回归待执行 | NOT_RUN |
| 设计稿逐页 | `docs/08-design/design-verification.md` | 静态页面核对；H5 页面已可见，认证核心状态本机浏览器复验；完整逐页和精确视口仍未完成 | PARTIAL |
| 后端测试 | `mvn test "-Dledger.audit.dev=true"` | 9 tests, 0 failures/errors/skipped；含 6 项审计单元测试、1 项真实 DEV、2 项金额测试 | PASS |
| 后端构建 | `mvn clean package "-Dledger.audit.dev=true"` | BUILD SUCCESS；DEV JAR 启动、Redis PING、MinIO Bucket 复验成功 | PASS |
| Java 25/Netty 兼容性 | `server/pom.xml`、依赖树、开发启动和 JAR 启动 | Netty 统一为 4.1.137.Final；两种启动方式均成功且应用日志无 Netty Unsafe 警告 | PASS |
| 前端依赖 | `pnpm install --frozen-lockfile` | Already up to date | PASS |
| 前端类型/构建 | `typecheck`、`build:h5`、`build:mp-weixin` | 三个命令均 DONE/exit 0 | PASS |
| 前端开发/生产环境 | `app/env/` 通用及环境模板、Vite envDir/loadEnv、生产缺失变量校验 | 通用/环境/进程变量覆盖断言通过；development H5、production H5/MP-WEIXIN 构建及生产地址注入通过；缺失 API 按预期失败；见 app-env-directory 迭代 | PASS |
| 前端 lint | `app/package.json` scripts | 未配置 lint script | NOT_RUN |
| DEV Profile/MySQL/Redis/MinIO | `application.yml`、本地忽略配置、启动日志 | 随 V1 重建后的应用随机端口启动成功；Flyway V1、Redis PING、MinIO Bucket 均实际成功 | PASS |
| 真实微信登录 | `wx.login` + code2session | 缺少可验证生产 AppID/域名 | BLOCKED |

## 2026-09-06 H5 空白修复复验

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| H5 运行 | localhost:5173 首次使用页可见，开始记账进入首页，日历/资产/我的可切换，平台重复导航已隐藏，复验无新增浏览器错误 | PASS |
| 类型与平台产物 | 最终 typecheck、H5 构建、微信构建 exit 0；H5 assets 20 个文件，微信 9 个页面 WXML 和 app.json | PASS |
| 生产配置保护 | 未配置 API 时按预期拒绝构建；临时本机 API 地址下两端构建成功 | PASS |
| 全量业务与设计 | 本次仅启动和导航验收，既有逐页设计及完整业务回归尚未完成 | PARTIAL |
| 微信平台运行 | 本次未执行微信开发者工具运行 | NOT_RUN |
| 真实生产构建配置 | 未提供真实生产 API 地址 | BLOCKED |

证据见 `../10-iterations/2026/09/h5-blank-page/`；上表更新此前 #app 空白结论，其他历史范围状态不由本次启动修复推定。

## 2026-09-06 Flyway V1 基线收口

当前完整数据库初始化以唯一的 `V1__init_schema.sql` 为准；旧 V2/V3/V4 已被完整吸收并从 Migration 目录移除。DEV `haji_dev` 已由显式保护的 Flyway clean 清理，再从空库 migrate V1、validate；最终 history 仅有一个成功的 SQL 版本 V1。六表字段、默认值、NULL 规则、中文 COMMENT、索引、外键、CHECK 约束及 Entity 映射验证 PASS，测试数据全部回滚。见 `../10-iterations/2026/09/flyway-v1-baseline/09-verification.md`。

本次范围外的完整业务 HTTP、前端、微信平台、Redis/MinIO 联动仍沿用各自既有状态，不由本次数据库收口推定。
