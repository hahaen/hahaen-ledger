# 第三轮工程审计报告

审计日期：2026-09-07。本次补充审计聚焦 H5 我的页、关于与帮助、用户累计天数、头像文件和退出登录，并追加首页、日历、资产三模块闭环；不把未执行的运行联调写成 PASS。

## 本轮结论

- 我的页与原型的个人卡片、累计天数、更多分组、关于与帮助、退出登录已落到 H5 页面。
- 累计记账天数由服务端 `ProfileService` 依据当前用户 `app_user.created_at` 计算，创建日计为第 1 天，并有单元测试证据。
- 头像上传复用现有 MinIO 预签名 URL 链路，文件查询补充当前用户和逻辑删除过滤；前端仅使用短时效预览 URL。
- H5 退出登录在 store 中清理服务端会话对应的本地令牌和业务状态，mine 页保持当前页面并显示“登录”。
- H5 刷新有效会话时不再无条件跳首页；Sa-Token 会话改用 Redis DAO，并对 Redis Key 增加 `haji:` 前缀。
- 用户启动日志发现 Redis DAO 自动配置与自定义 DAO 重复注册；已通过排除自动配置保留单一自定义 DAO，避免 Spring 上下文启动失败。
- 前端 typecheck、H5 构建、后端编译和测试 PASS；真实 H5 登录态刷新为 PARTIAL，MySQL 账务联调和微信开发者工具验收仍为 BLOCKED/NOT_RUN；Redis PING 与 MinIO bucket 探针已在独立端口启动时成功，但真实对象/账务链路未执行。

## 本轮追加：首页、日历、资产模块

- 后端新增账户、资产、账单、退款、首页汇总和日历查询域，严格映射既有 V3/V4 表；写操作包含当前用户归属、逻辑删除、金额分、账户类型、幂等和事务边界。
- 首页已支持月度切换、服务端汇总、有效退款金额、按日倒序分组、空/错/加载态；日历已改为周日开始的 42 格月历，支持今日/选中/非当月日期、月度标记和单日接口；资产页由后端返回净资产、总资产、总负债，并支持资金/信贷分组、账户表单、流水筛选和部分还款。
- 新增 `AccountServiceTest`、`AssetServiceTest`、`TransactionServiceTest`、`HomeServiceTest`、`CalendarServiceTest` 和 `GlobalExceptionHandlerTest`；最终 Maven 18 tests 全部通过。
- 独立端口 18080 在 `spring.flyway.enabled=false` 下启动成功，Redis PING、MinIO bucket 探针成功，未登录账户接口返回 401，OpenAPI 暴露本轮 12 个路径。
- 本轮未执行 Flyway/DDL，Migration diff 为空；没有隔离测试用户和真实微信会话，因此真实账单写入、跨用户/并发事务、`information_schema` 对照和微信开发者工具验收保持 BLOCKED/NOT_RUN。

## 本轮差异与待确认

1. V3 没有独立账户停用状态，当前账户“删除/停用”统一落为 `deleted=1`；历史账单仍保留，但删除账户详情不再作为有效账户访问。
2. V3 没有余额补齐流水的业务标识，编辑余额当前只更新余额列，没有伪造一条会污染统计口径的普通账单；若必须保留校准流水，需要后续数据库设计变更。
3. V3 没有账户名称唯一索引；本轮在 Service 层按当前用户和有效账户做名称重复校验，但无法替代数据库级并发唯一约束。
4. AI 设计稿和原型的逐区域截图、微信安全区和平台交互尚未完成，状态不是 PASS。

## 仍需人工验收

1. 启动 MySQL、Redis、MinIO，使用真实 H5 会话验证刷新保留当前路由、后端重启续会话、个人资料、头像上传/刷新、旧预览 URL 过期重取和退出后的“登录”状态。
2. 在 320/375/414px 下截图对照原型，检查滚动、安全区和帮助页长内容。
3. 后续补齐微信小程序头像选择和平台差异，不把 H5 文件选择器直接移植到微信端。

## 本轮追加：资产账户数据库设计

- 已新增 V3 `asset_account` 统一账户表，使用 `FUND`/`CREDIT` 区分资金账户和信贷账户，并用数据库约束维护金额字段的类型对应关系。
- 账户表包含用户归属、净资产标识、完整公共审计字段、逻辑删除字段和资产页查询索引；账户名称允许重复，当前单账本模型不重复保存账本字段。
- 本轮仅完成 SQL 与文档；未修改 Java、TypeScript 或业务接口，也未实际执行 Flyway/MySQL。数据库执行和 `information_schema` 对照保持 `NOT_RUN`，不能据此宣称运行通过。

## 本轮追加：账单明细与退款数据库设计

- 已新增 V4 `transaction_detail` 账单明细主表和 `transaction_refund` 独立退款关联表。
- 主表使用 `EXPENSE`、`INCOME`、`TRANSFER`、`REPAYMENT` 四类账单；退款不占用账单类型，并以 `transaction_id` 关联原始账单。
- 金额使用整数分，保留 `original_amount`，以 `amount` 表示有效金额，并使用 `has_refund` 记录是否曾发生退款；V4 静态约束覆盖金额范围、字段适用形状、编号唯一性和查询索引。
- 当前为单账本模型，V4 不在 `transaction_detail` 重复保存 `book_id`；用户/账户归属和退款累计事务由后续 Service 完成。
- V4 尚未在 MySQL/Flyway 中执行，`information_schema` 对照和退款运行时事务用例为 `BLOCKED`/`NOT_RUN`，详见 `docs/10-iterations/2026/09/transaction-detail-refund-schema/09-verification.md`。

## 本轮追加：原型样式还原

- 已依据 `哈记账小程序_原型设计稿/app.js`、`styles.css` 和完整功能说明，把 app 页面统一到原型的薄荷绿令牌、卡片层级、公共页头、账单行、底部导航、悬浮新增按钮和安全区规则。
- 记账页恢复固定四列四行金额键盘，账户、日期、备注使用居中弹窗；月份选择使用双列年份/月选择器；账户详情与账单详情补齐原型摘要卡和底部操作栏。
- 视觉验收使用独立只读服务 `app/tests/visual-server.mjs`，不连接真实后端，保存了 375px 基准截图和 320/414px 截图；`layout-results.json` 显示 11 个页面均无横向溢出。
- `pnpm run typecheck`、H5 生产构建和微信小程序构建均为 PASS。真实数据库/缓存/对象存储和微信开发者工具人工验收仍为 BLOCKED/NOT_RUN。

## 本轮追加：登录/注册验证码布局修复

- 根因是 uni-app H5 的 `button` 按内容收缩，验证码按钮实际宽约 13.33px，内部百分比图片宽度为 0；SVG 本身已成功解码。
- `.captcha-button` 已明确设置 `display:flex`、`width:100%`、`box-sizing:border-box`，登录和注册共用修复。
- 本地 H5 已验证登录/注册刷新行为及 320/375/414px 横向布局；类型检查与两类生产构建均 PASS。真实后端和微信工具联调仍按 BLOCKED/NOT_RUN 记录。

## 文档真实性审计追补（2026-09-08）

本节是对本报告的后续审计记录，不改写前述 2026-09-07 结论或当时的历史上下文。

- 当前工作区真实执行 `server/mvn test`：18 tests，0 failures/errors/skipped；前端 `pnpm run typecheck`、H5 构建和微信小程序构建均通过。
- 当前运行探针可访问 `/api-docs` 和 `/api/app/auth/captcha`，未登录访问 `/api/app/accounts` 返回 401；这只证明当前运行实例的协议探针，不证明真实 MySQL 账务事务。
- 发现规范与当前代码的关键差异：小程序 Store 仍调用 `/api/app/auth/login`，但当前 `AuthController` 只有 H5 登录/注册路径；微信认证应标记为接口未闭环。
- 发现 `asset_account` 数据库没有账户名称唯一索引，但当前 `AccountService` 对有效账户执行重名校验；当前规范已明确这是 Service 约束并列出并发竞态风险。
- 发现 `docs/09-audit/README.md` 原引用不存在的 `second-round-audit.md`，已改为说明历史明细未保留；当前工作区也未找到报告曾引用的 `app/tests/evidence/*.png`，因此视觉截图不再作为本次可复核 PASS。
- 当前无可复核的 MySQL 3306、Redis 6379、MinIO 9000 运行证据，Flyway history、`information_schema`、真实对象链路、有效 H5 会话刷新和微信开发者工具保持 `BLOCKED`/`NOT_RUN`。
