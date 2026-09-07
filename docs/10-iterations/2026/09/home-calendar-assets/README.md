# 首页、日历、资产模块闭环迭代

本迭代在不修改任何数据库表结构、Flyway Migration、构建配置和部署配置的前提下，补齐首页、日历、资产及其必要的账单/账户关联流程。

## 任务矩阵

| 编号 | 页面区域/行为 | 现状与差距 | 数据/接口 | 后端规则与测试 | 状态 |
| --- | --- | --- | --- | --- | --- |
| HOME-01 | 首页月度概览 | 已补齐月份参数、服务端汇总、错误/空/加载态；真实登录数据联调未执行 | `transaction_detail`、`GET /api/app/home/summary` | 用户、逻辑删除、自然月边界；`HomeServiceTest` | PARTIAL |
| HOME-02 | 最近记账分组、排序与金额 | 已补齐有效金额和四类账单展示；真实数据联调未执行 | `GET /api/app/transactions`、`transaction_refund` | 支出/收入使用有效金额，转账/还款不进入收支；`TransactionServiceTest` | PARTIAL |
| HOME-03 | 月份切换与刷新 | 首页已提供月份切换和失败重试，Store 使用请求序列保护 | summary month 参数 | 月份校验；`HomeServiceTest`、构建验证 | PASS（代码/构建） |
| HOME-04 | 详情/新增联动 | 新增/编辑/删除/详情/退款接口和页面已接通 | transaction/account 表 | 归属、幂等、余额一致性；`TransactionServiceTest` | PARTIAL |
| CAL-01 | 完整月历网格 | 已补齐周日开始的42格月历、大小月和闰年 | `GET /api/app/calendar` | 月首偏移和闰年；`CalendarServiceTest` | PASS（代码/测试） |
| CAL-02 | 月度标记与选中日 | 已补齐当日标记、今日、选中态和非当月日期 | 月度日期聚合 | 当前用户有效账单过滤；`CalendarServiceTest` | PASS（代码/测试） |
| CAL-03 | 单日汇总与列表 | 已补齐单日接口、收支汇总、列表和详情跳转 | `GET /api/app/calendar/{date}` | 日期边界和统计口径；`CalendarServiceTest` | PARTIAL |
| CAL-04 | 快速切月/切日 | 前端月份请求有序号保护，旧响应不会覆盖新月 | 前端请求序列号 | typecheck/H5/微信构建 | PASS（代码/构建） |
| AST-01 | 净资产/总资产/总负债 | 概览改由后端资产接口返回，前端不再冒充计算 | `GET /api/app/assets/overview` | `include_net_asset`、资金余额、信贷欠款；由账户映射测试覆盖 | PARTIAL |
| AST-02 | 账户分组、排序、空态 | 服务端按类型/名称排序，前端资金/信贷可收起并有空态 | `GET /api/app/accounts` | `deleted=0`、账户类型；`AccountServiceTest` | PARTIAL |
| AST-03 | 账户新增/编辑/删除 | 已补齐校验、保存禁用、错误反馈和主动逻辑删除 | account CRUD | 名称/金额/额度、归属、软删除；`AccountServiceTest` | PARTIAL |
| AST-04 | 账户详情与流水筛选 | 已补齐详情表单、类型筛选、分页契约和历史流水展示 | account detail + transactions | 账户归属与 `deleted=0`；`AccountServiceTest` | PARTIAL |
| AST-05 | 信贷还款 | 已补齐部分还款、资金/欠款校验、幂等键和还款流水 | `POST /api/app/accounts/{id}/repayments` | 同事务更新两账户和还款账单；`TransactionServiceTest` | PARTIAL |
| TX-01 | 支出/收入/转账/还款写入 | 已补齐四类账单 Service/Controller/前端表单 | `transaction_detail` | 账户类型、金额分、日期、备注、幂等；`TransactionServiceTest` | PARTIAL |
| TX-02 | 账单编辑/删除 | 已补齐撤销旧影响、应用新影响和主动软删除 | transaction update/delete | 余额与逻辑删除；`TransactionServiceTest` | PARTIAL |
| TX-03 | 退款创建/删除 | 已补齐父账单行锁、累计上限、有效金额重算和逻辑删除 | `transaction_refund` | Service 单测覆盖退款余额联动，真实事务未跑 | PARTIAL |
| TX-04 | 详情与关联查询 | 已补齐用户范围、逻辑删除、退款过滤和账户名称回显 | transaction detail | 不信任前端 userId；`TransactionServiceTest` | PARTIAL |
| TEST-01 | 纯逻辑与参数 | 金额、日期、月历边界和金额分测试已执行 | 金额、日期、月历 | 17 个 Maven 测试全通过 | PASS |
| TEST-02 | Service 业务规则 | 账户/账单/退款/Home/Calendar 测试已编写并执行 | 账户/账单/退款 | 正常、非法、越权、余额和闰年场景 | PASS（单测） |
| TEST-03 | Controller 协议 | Springdoc 路径与未登录 401 已验证；完整 MockMvc 参数矩阵未补齐 | DTO 校验与认证 | `GlobalExceptionHandlerTest`、18080 HTTP 证据 | PARTIAL |
| TEST-04 | 集成环境证据 | 独立端口启动、Redis/MinIO 探针、OpenAPI/401 已验证；未登录真实账务数据未执行 | Maven/前端构建 | 无隔离测试用户/真实微信会话，避免污染开发数据 | BLOCKED |

## 依赖顺序

1. 读取 V3/V4 的现有字段并建立 Entity/Mapper/DTO/VO，不修改 SQL。
2. 完成账户与账单服务的归属、金额、余额、幂等、逻辑删除和退款规则。
3. 增加首页、日历、资产 Controller 及接口协议。
4. 补齐前端 API 类型、状态刷新、月历交互、账户表单和异常状态。
5. 编写并执行自动化测试，最后执行前端类型检查、H5/微信构建和后端构建。
6. 将实际命令、证据和 BLOCKED 项回填本迭代及 `docs/09-audit`。

## 禁止修改范围

- `server/src/main/resources/db/migration/**`：本迭代不修改任何 Migration。
- `server/pom.xml`、`app/package.json`、构建、部署和环境配置：本迭代不修改。
- 不执行 DDL、Flyway 写入或 Git 写操作。
