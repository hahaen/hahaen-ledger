# 核心页面原型对齐与记账流程修复

日期：2026-09-08。范围：首页、日历、新增/编辑记账、账单详情、帮助页未开放反馈入口。资产、我的页面文件无差异；未改其结构与交互。

## requirement
依据根目录完整功能说明、AI 设计需求及原型目录 app.js/styles.css，沿用 Vue 3、uni-app、TypeScript 和统一 API 封装，不增加分类或凭证。

发现的问题：展示金额含千分位导致千元以上编辑/默认退款失败；编辑加载失败仍可提交；重复点击和成功返回前的重复写入窗口；刷新失败被误报成写入失败；日历旧日期数据闪现、返回丢失选中日期；缺少还款编辑；退款删除存在等待锁后的重复扣款窗口；帮助反馈入口未明确未开放。

## design
沿用原型薄荷绿汇总卡片、原有图标、居中弹窗和四列键盘。首页汇总标题增加月份提示，收支分列、分组间距、刷新入口和加载占位；日历增加选日新增、空态入口和独立日加载状态；详情长备注/编号换行及退款后有效金额；记账字段禁用状态、错误提示和单项还款类型布局。

业务冲突处理：原型货币符号遵循当前金额规范继续省略；分类/凭证/统计不作为首版可用能力。完整说明要求还款可编辑，修复原实现隐藏入口的问题，新增记账仍只有支出/收入/转账，还款创建仍从现有资产流程进入。已有退款的账单按后端规则锁定类型与账户，原始金额不得小于累计退款。

## database
无 Schema 变更，不修改 Flyway、公共审计字段或历史 Migration。退款删除增加锁内当前读，使用原账单锁串行化同一账单的退款变更。

## api
沿用 /api/app/transactions、/refunds、/home/summary、/calendar 与账户接口，全部通过 utils/api.ts。增加 TransactionPayload 类型；整数分请求不含展示分组符。编辑不覆盖创建幂等键，退款相同幂等键但金额不同返回业务冲突。

## backend
编辑先撤销旧余额影响再校验新还款，全部位于原 @Transactional 边界内；退款删除在取得原账单锁后再次 FOR UPDATE 读取有效退款，避免重复影响余额；记账年份校验 1000–9999。保持当前用户归属及 deleted=0 过滤。当前项目没有独立 book 实体，本次不扩大为多账本重构。

## frontend
inputYuan 用于无分组输入，formatYuan 继续用于展示。计算器用安全整数分数计算、最后舍入到分；超出安全计算范围明确报错。增加实际日期、备注长度、金额和账户校验；编辑初始化未完成或失败时禁用保存并可重试；新增重试复用创建幂等键；成功后返回前继续锁定按钮。详情退款重试复用幂等键，删除/退款共享操作锁；失效链接可重试或返回首页。首页使用独立月份快照，日历使用月/日请求序号拒绝过期响应并保留选日。写入与后续刷新分开处理。启动网络失败不再直接清除有效会话，401 仍由统一封装处理。

## testing
前端 9 个回归场景：输入展示往返、计算器优先级/舍入、非法日期、编辑加载失败禁写、连续保存只写一次、失败幂等重试、选日新增、日请求竞态、写入成功刷新失败。后端 24 项单测（交易 9 项），新增还款编辑、重复编辑余额、创建幂等键保留、退款冲突、重复退款删除和非法日期覆盖。

## commands
| 工作目录 | 实际命令/检查 | 结果 |
| --- | --- | --- |
| app | node --test tests/entry.test.mjs tests/page-flows.test.mjs | PASS：9/9 |
| app | pnpm run typecheck | PASS |
| app | 临时设置 VITE_API_BASE_URL=http://127.0.0.1:8080 后 pnpm run build:h5 | PASS；仅用于本地验收的构建地址 |
| app | 同样临时设置后 pnpm run build:mp-weixin | PASS |
| server | mvn test | PASS：24 tests，0 failures/errors/skipped |
| server | mvn package -DskipTests（单测已单独执行） | PASS：生成 Spring Boot JAR |
| 仓库 | git diff --check | PASS；仅换行归一化提示 |
| 仓库 | git diff -- app/src/pages/assets/assets.vue app/src/pages/mine/mine.vue | PASS：无差异 |
| 本地探针 | GET http://127.0.0.1:8080/api/app/accounts（未登录） | PASS：HTTP 401 |
| app | node tests/visual-server.mjs | PASS：18761 只读视觉环境，拒绝写入且不连接真实后端 |

构建有现有 Sass legacy-js-api 警告；Maven/JDK 有现有 native-access、Unsafe、Mockito 动态 agent 警告，均未导致失败。读取期间曾有一次 Select-Object 参数拼写错误和一次测试服务器脚本路径错误，已纠正；它们不作为功能验证结果。

## verification
- PASS：上述自动检查及浏览器只读交互；首页/日历/记账/详情在 320、375、414px 检查，320/414 的 documentWidth 等于 viewport，见 evidence/layout-checks.json。
- PASS：375px 截图及实际操作验证千元编辑回填、默认退款金额、失败保留输入与可重试、返回保留日历 9月14日、选日带入新增，以及还款编辑账户回填。截图使用明确的测试数据，不代表真实账务已写入。
- PARTIAL：前后端核心流程代码和回归已完成，真实认证后的数据库端到端尚未闭环。
- BLOCKED：本次浏览器没有可用登录会话/测试账号，无法开展真实账务新增、编辑、退款、删除联调。未擅自创建账户、读取凭证或操作现有个人账务。
- NOT_RUN：真实 MySQL/Flyway/Redis/MinIO 联调与数据库并发事务，微信开发者工具真机、微信登录。当前微信登录后端路由缺失为已有待办。
- FAIL：最终已执行验证无失败项。

## rollback
撤回本迭代涉及的首页/日历/entry/detail/help、entry/money 工具、ledger 刷新逻辑、App 启动异常处理、限定页面样式、退款锁读取及交易服务修改，并撤回对应测试/文档；无数据迁移。不要撤回资产或我的页既有实现。此说明不执行任何 Git 写操作。

## 证据
截图与响应式记录位于 [evidence/](evidence/)。只读示例服务器更新为当前字符串 ID 契约并添加还款样例，不进入正式产物。
