# `third-round`｜第三轮综合审计

## 2026-09-30 追加：生产通知密钥示例

生产 env 示例补上 `NOTIFICATION_KEY_AES_KEY` 占位项；应用配置映射与 Compose `env_file` 路径静态核对 PASS。生产服务器上的真实配置和运行 NOT_RUN。[通知中心迭代](../notification-center/README.md)。

## 2026-09-30 追加：通知中心顶部卡片

通知中心增加个人中心同款薄荷渐变引导卡，复用公共样式和表单间距。类型检查、H5/微信生产构建及实际 H5 画面核对 PASS；微信工具/真机视觉 NOT_RUN。[追加档案及截图](../notification-center/README.md)。

## 2026-09-30：通知中心与 Key 回显

通知配置已移出个人中心，独立入口紧跟“我的”页个人中心；GET 按当前有效用户解密回显 Key 并设置禁止缓存。6 项通知定向/DEV 集成测试、前端类型检查、微信转发18项与双端构建 PASS；H5 实际入口顺序及空配置画面 PASS，真实页面保存/再次进入及微信交互 PARTIAL/NOT_RUN。详情见 [本轮档案](../notification-center/README.md)。


## 2026-09-29 追加：通用通知配置

V9 新增按用户与通知类型唯一的通知配置主表和幂等关系表；个人中心新增 Bark、pushplus 可选 Key。RSA-OAEP 传输、AES-256-GCM 入库，接口不回传 Key。DEV Flyway V9、隔离 HTTP/数据库闭环、6 项定向/集成测试、前端类型检查和 H5/微信构建 PASS；后端普通全量测试因旧日志路径断言 FAIL；H5/微信实际页面验收及真实消息投递 NOT_RUN。详见 `../notification-config/README.md`。

## 2026-09-29 追加：物品清单实际净成本

清单行价格改为读取 `netCostCents`。前端物品回归 5/5、Java 25 定向测试、类型检查与 H5/微信构建 PASS；H5 只读示例看到退役盈利物品净成本 `-200 元`。真实登录态与微信页面未验收，详见 `../item-list-net-cost/README.md`。

## 2026-09-29 追加：已退役物品重新服役

物品详情新增确认操作，状态恢复在役并清空旧退役日期与二手价格。V8 已在 DEV 执行；14 项 Service 测试、隔离合成用户 HTTP/数据库闭环、V8 关系表结构、前端全量 87 项测试、类型检查和双端构建 PASS。后端全量 68 项中 1 项历史日志路径测试在 macOS 失败；Chrome 点击控制超时，H5 实际交互及微信工具/真机 NOT_RUN。详见 `../item-reactivation/`。

## 2026-09-29 追加：物品价格输入格式

新增及编辑购买价格输入框占位改为 `0`，资料编辑使用 `inputYuan` 回显；定向回归 9/9、类型检查与 H5/微信构建 PASS。真实 H5 登录态和微信工具/真机视觉 NOT_RUN。专项记录见 `../item-price-input-format/`。

## 迭代目的

本迭代用于对需求、架构、数据库、API、权限、金额、事务、幂等、缓存、文件、日志、测试、构建和文档进行一次跨层闭环审查。

## 应记录的重点

审计结论必须逐项绑定代码、Migration、命令输出、接口回归或截图证据，并严格区分 PASS、PARTIAL、FAIL、BLOCKED 和 NOT_RUN。重点关注“页面存在但业务不可用”“构建通过但依赖未联调”“前端传 userId”“逻辑删除未过滤”等假通过风险。

## 当前档案状态

本轮新增的 H5 我的页及会话持久化修复审计结论已同步到 `docs/09-audit/verification-matrix.md` 和 `docs/09-audit/third-round-audit.md`；未执行的真实基础设施联调仍按 BLOCKED/NOT_RUN 记录。

## 2026-09-28 追加：移除全局点击振动

普通点击的系统振动已移除，金额键盘轻振动保留；Node 回归 76/76、TypeScript、H5/微信构建和产物静态核对 PASS。真实设备振动验收为 NOT_RUN。详细档案：`../global-click-vibration-remove/`。

## 2026-09-19 追加：新增记账页 H5 触摸滚动

H5 条件编译样式已为 `.entry-content` 增加 iOS Safari 惯性触摸滚动，微信小程序样式与固定数字键盘未改。自动化测试、构建和 iOS Safari 真机触摸验收均为 NOT_RUN；详细记录见 `../h5-entry-touch-scroll/README.md`。

## 2026-09-20 追加：新增与编辑记账页 H5 手机滚动边界

针对上一轮仅增加惯性滚动仍无法覆盖用户手机现象的问题，H5 `.entry-content` 明确设置 `flex:1 1 0`、`height:0`、`min-height:0` 与 `overflow-y:auto`，固定数字键盘和微信小程序样式未改。布局静态回归、TypeScript 和 H5 构建已执行；只读 H5 手机尺寸夹具确认主体滚动高度大于可视高度。真实 iOS Safari、真实登录账务数据和微信开发者工具仍未执行，详见 `../h5-entry-page-scroll/README.md`。

## 2026-09-19 追加：账户负余额与信贷支出

资金账户支出、转账和还款允许低于 0；支出可选择信贷账户，超出可用额度时显示提示但仍可保存，溢缴可由负欠款表达。前后端测试、类型检查及 H5/微信构建状态以后续 `../negative-account-balances/08-commands.md` 为准；Flyway/MySQL、DEV API、登录态页面和真机验收仍需实际环境证据。详见 `../negative-account-balances/README.md`。

## 2026-09-19 追加：微信小程序全页面自定义导航对齐

共享导航组件现按微信胶囊实际矩形布置主页面品牌行与次级页面标题栏，并消除状态栏/安全区重复留白。前端回归 55/55、TypeScript、H5/微信小程序构建和产物静态核对 PASS；微信开发者工具/真机画面仍为 NOT_RUN。详细记录：`../wechat-custom-navigation-alignment/README.md`。

## 2026-09-19 追加：MinIO 新上传对象 Key 命名

新头像对象 Key 改为 `avatars/<上海时区yyyyMMdd>/<userId>/<原文件名主干>-<UUID>.<内容MIME扩展名>`；用户归属继续由 `app_file.user_id` 和当前用户查询校验，历史对象 Key 不迁移。静态代码与文档核对 PASS；自动化测试、构建及真实 MinIO 上传/读取未运行，详见 `../minio-date-filename-object-key/README.md`。

## 2026-09-17 追加：新增记账页微信小程序顶部控件

新增记账页已将自定义顶部导航移出滚动层；小程序专用样式预留微信胶囊导航区域，并显式约束支出/收入/转账按钮。前端回归 53/53、类型检查、H5/微信小程序构建和小程序产物静态核对为 PASS；真实微信开发者工具画面与点击验收仍为 BLOCKED/NOT_RUN。

## 2026-09-17 追加：微信小程序个人中心底部按钮可见性

个人中心保存按钮和修改密码确认按钮已补充微信原生 `button` 的明确颜色及伪元素清理；头像已改为 `uni.chooseImage` 用户自行选择并上传，保留预览后保存关联且不获取微信头像资料。前端回归 52/52、类型检查、H5/微信小程序构建和小程序产物静态核对为 PASS；真实微信开发者工具画面与点击验收因当前 UI 自动化窗口不可控，仍为 BLOCKED/NOT_RUN。

## 2026-09-17 追加：微信小程序主题色跨端兼容

共享样式中主题色实际引用已改为明确静态颜色值，覆盖首页、日历、资产、我的、底部导航、页头和复用弹层/表单，避免微信小程序端主题变量解析失败后回退为黑色。前端回归 54/54、类型检查、H5/微信小程序构建、WXSS 静态检查和 `git diff --check` 为 PASS；真实微信开发者工具页面画面与点击仍为 BLOCKED/NOT_RUN。

## 2026-09-13 追加：HTTP 临时认证兼容

H5 认证在浏览器缺少 Web Crypto 时，可以在服务端默认关闭的显式开关下提交 `compatibilityPassword` 混淆载荷；HTTPS 仍使用 RSA-OAEP，个人中心改密不参与 HTTP 兼容。定向后端测试 4/4、后端全量 43/43、前端流程 30/30、类型检查和 H5 构建均通过；真实服务器 HTTP/HTTPS 认证仍为 NOT_RUN。该载荷无法抵抗 HTTP 中间人，HTTPS 部署后必须关闭开关。详见 `../http-temporary-auth-compatibility/`。

## 本轮追加：Jenkins 流水线兼容性

Jenkins 已确认能够从 Gitee 获取 `server/Jenkinsfile`。由于当前控制器未安装 Timestamper 插件，`timestamps()` 在 Declarative Pipeline 解析阶段失败；该非必要选项已从前后端流水线移除。重新提交并触发构建前，SSH、Docker Compose 和服务健康检查保持 NOT_RUN；详见 `../jenkins-compose-deployment/09-verification.md`。

## 本轮追加：Nginx 入口配置说明对齐

`deploy/nginx/haji-api-location.conf.example` 已同步为完整虚拟主机示例：`map` 放在 `http {}` 上下文；`/jenkins/` 保留前缀代理到 Jenkins；`/haji-api/` 去除公网前缀后代理到后端既有 `/api/...`；其他路径提供 H5 SPA 回退。仓库文本核对为 PASS（静态），服务器上的 `nginx -t`、reload 和真实 HTTP 访问尚未执行，状态为 NOT_RUN。

## 本轮追加：账单明细与退款数据库设计

本轮追加了 V4 账单明细与退款数据库设计，具体迭代档案为 `../transaction-detail-refund-schema/`。静态 Migration、命名、注释、金额和索引核对为 PASS；由于本机没有可用 MySQL，Flyway 实际执行及 `information_schema` 对照为 BLOCKED，Java Service 事务实现按本次范围为 NOT_RUN。

## 本轮追加：认证页说明文字移除

登录/注册共用页面已移除微信自动登录和注册资料说明文字，认证字段及既有 H5 账号认证流程保持不变。对应静态核对和前端构建结果以 `docs/09-audit/` 当前记录为准。

## 本轮追加：H5 浏览器主应用图标

`app/index.html` 已声明已有 `app/static/brand.png` 为浏览器 favicon。本机开发入口资源和 H5 生产构建产物均已核对；真实生产域名/CDN 缓存验收保持 NOT_RUN。详细记录见 `../h5-favicon/`。

## 本轮追加：资产新增弹窗设计对齐

资产页右下角新增入口已改为原型风格的新增资产账户表单弹窗，支持资金/信贷账户切换和现有账户创建接口。详细记录见 `../asset-create-modal-design/`。类型检查和临时设置本地 API 地址后的 H5 构建 PASS，视觉截图和微信工具验收保持 NOT_RUN。

## 本轮追加：账户 ID 精度修复

资产账户、流水、退款及相关登录/文件响应的实体 ID 已统一以字符串返回，前端路由参数和本地缓存也统一按字符串处理，修复 64 位雪花 ID 在 JavaScript 中精度丢失导致的详情查询失败。数据库和金额计算未修改；前端类型检查及 19 项后端单元测试 PASS，详细记录见 `../account-id-precision-fix/`。

## 本轮追加：账户详情布局

账户详情页已压缩顶部导航；“停用账户”改为“删除账号”但仍使用逻辑删除；编辑账户复用新增弹窗布局并隐藏 Tab。前端类型检查和 H5 构建 PASS，详细记录见 `../account-detail-layout/`。

## 本轮追加：还款弹窗与账户选择

还款流程已改为参考稿风格的自定义弹窗，并增加资金账户选择弹窗；金额校验、幂等键和既有还款接口保持不变。详细记录见 `../repayment-modal-account-picker/`。

## 前端金额展示统一

已在 ../money-display-unification/ 完成前端金额展示统一：复用 money.ts 的公共格式化实现，覆盖首页、日历、资产、账户、账单、退款、还款、交易列表和记账编辑/计算；.00 不展示，有效小数保留两位。类型检查、H5/微信小程序构建和边界样例 PASS；真实会话视觉验收保持 NOT_RUN。

## 金额图标移除

已在当前修订中完成运行时前端钱币图标移除：MoneyDisplay 公共组件覆盖首页、日历、资产、账户详情、账单详情、退款、还款、记账和交易列表，仅展示金额数值。资产、账户、还款和记账输入框同步移除 `¥` 前缀；类型检查、H5/微信小程序构建和只读视觉核对 PASS，真实微信开发者工具会话 NOT_RUN。

## 本轮追加：核心页面原型与记账流程

[core-pages-prototype](../core-pages-prototype/README.md) 完成首页、日历、记账/还款编辑、详情/退款和帮助未开放提示。前端 9 项回归、后端 24 项单测、类型检查、双端构建及只读视觉核对 PASS；真实数据库端到端 PARTIAL，登录会话 BLOCKED，微信工具 NOT_RUN。

## 本轮追加：编辑记账类型固定（2026-09-09）

账单详情进入编辑记账后，类型由账单接口回填并固定；编辑态隐藏新增记账的支出/收入/转账切换器，并在状态层拒绝编辑态类型切换。新增态行为未改变。15/15 前端回归、类型检查、H5 和微信小程序生产构建 PASS；真实登录账单操作和微信开发者工具人工验收 BLOCKED。详细记录见 `../transaction-edit-type-lock/`。

## 本轮追加：V5 启动校验修复（2026-09-08）

PASS：移除 V5 首行误加的 cd，恢复数据库既有 checksum 266153221；mvn test package 25 项测试通过。实际 DEV 启动验证 5 个迁移全部通过、Schema 保持 V5 且无需迁移、应用成功启动；临时 18080 未登录账户接口返回 401，验证后停止实例。未执行 repair 或修改数据库历史。记录：docs/10-iterations/2026/09/flyway-v5-checksum/README.md。

## 首页交互修正（2026-09-08）

首页“当前年/月 · 日均消费”为普通文字，不提供月份选择；最近记账右侧不显示刷新按钮。年月与查询范围在加载时按当前日期更新。类型检查、H5 构建 PASS。档案：docs/10-iterations/2026/09/home-static-month/README.md。

## 首页摘要布局与当前月份核对

已修复首页日均消费金额与标题同一行的样式覆盖问题，并确认首页年月在初始化和加载时按当前日期动态取得。类型检查、H5 构建和只读视觉核对 PASS；真实认证账务与微信工具验收仍未执行。档案：docs/10-iterations/2026/09/home-summary-layout-current-month/README.md。

## 账单详情顶部对齐

账单详情页顶部已与新增记账页采用同一紧凑导航规格。前端类型检查、H5 构建和只读视觉夹具核对 PASS；有效 H5 登录态下的真实详情页核对为 BLOCKED。详细记录见 `../transaction-detail-header-alignment/`。

## 账单详情删除弹窗样式统一

账单详情页账单删除和退款删除均已改用账户详情页同款自定义圆角确认弹层；前端类型检查、H5/微信小程序构建 PASS，真实登录态视觉验收为 NOT_RUN。详细记录见 `../transaction-detail-delete-modal/`。

## 账单详情退款弹窗设计对齐

退款弹窗已改为账单详情专用的系统风格圆角弹层，增加顶部把手、说明、放大的金额输入框和双大按钮；打开时默认回填剩余可退款金额，退款 API、整数分、幂等和错误重试保持不变。前端 15 项回归、类型检查、H5/微信小程序构建 PASS；Codex 内置浏览器访问本机视觉夹具被 `ERR_BLOCKED_BY_CLIENT` 拦截，真实登录态退款验收为 NOT_RUN。详细记录见 `../transaction-refund-modal-design/`。

## 本轮追加：退款记录删除失败修复（2026-09-10）

退款记录删除成功后，前端立即移除目标记录并同步回算累计退款、有效金额和退款状态，详情刷新与账户刷新解耦，且始终释放保存状态；详情首次加载等待路由 ID，避免生命周期顺序变化时请求空账单。后端 `deleteRefund()` 继续事务性执行软删除和余额恢复。前端回归 18/18、Maven 26/26、类型检查和双端生产构建 PASS；真实登录态与 MySQL 实际事务验收仍为 NOT_RUN/BLOCKED。

## 日历页独立滚动（2026-09-10）

日历页根容器锁定视口高度并隐藏页面溢出，页头、月历卡片、当日汇总和日期标题固定；选中日期的记账记录改为独立纵向 `scroll-view`。布局回归、既有前端回归共 19/19、类型检查和 H5 生产构建 PASS；独立只读夹具观测到页面 `scrollTop=0`、记录滚动节点可滚动。PNG 截图和微信开发者工具人工验收为 NOT_RUN。详细记录见 `../calendar-records-scroll/`。

## 账户详情固定筛选与流水独立滚动（2026-09-10）

资金账户和信贷账户共用的账户详情页已固定账户卡片、流水标题及“全部/支出/收入/转账/还款”筛选按钮，日期/笔数行和流水内容放入独立纵向 `scroll-view`。布局回归、既有前端回归共 20/20、类型检查和 H5 生产构建 PASS；独立只读夹具验证资金账户页面自身 `scrollTop=0`、流水滚动节点可滚动。PNG 截图和微信开发者工具人工验收为 NOT_RUN。详细记录见 `../account-records-scroll/`。

日期分组标题补充 `position:sticky` 吸顶规则：本组流水滚动时标题保持在记录区顶部，下一分组顶上来后自然替换。

## 2026-09-17 追加：微信小程序日历选中日期可见性

日历日期格和日期数字已从原生 `button`/`text` 调整为跨端 `view`，并强制设置选中数字的 `#49ad9c` 背景、`background-color` 和白色文字，避免微信小程序端回退为白色。前端 53/53 回归、类型检查、H5/微信小程序构建和小程序产物静态检查 PASS；微信开发者工具真实页面、登录态点击和截图仍为 BLOCKED/NOT_RUN。详细记录见 `../calendar-mini-selected-state/`。

## 2026-09-17 追加：新增记账退出不提示放弃修改

新增记账退出不再显示放弃确认，输入金额、备注、选择账户和类型切换均不会触发弹层；编辑已有账单仍保留未保存修改保护。前端 Node 回归 55/55、类型检查及 H5/微信小程序生产构建 PASS；真实登录态下的退出操作仍为 NOT_RUN。详细记录见 `../transaction-create-exit-no-discard/`。

## 2026-09-20 追加：全局按钮与可点击控件轻触反馈

共享样式增加两端一致的轻微按压反馈；H5 捕获式调用浏览器轻振动，微信 App 外层捕获式调用 uni.vibrateShort。前端 Node 回归 60/60、类型检查和 H5/微信小程序生产构建 PASS；真实 H5 移动浏览器及微信开发者工具/真机物理振动验收 NOT_RUN。详细记录见 `../tap-feedback/`。

## 2026-09-20 追加：微信小程序个人中心密码加密兼容

个人中心在微信小程序缺少浏览器 Web Crypto 时改由平台工具层生成同规格 RSA-OAEP/SHA-256 密文，随机种子只来自 `wx.getRandomValues`，不降级为明文或弱随机数。专测 2/2、TypeScript、H5/微信小程序生产构建、后端 53/53 和产物静态核对为 PASS；全量前端回归因 2 项既有 mine 模板断言失败为 PARTIAL；真实微信首次设置/修改密码及数据库落库为 NOT_RUN。详细记录见 `../wechat-mini-password-encryption/`。


## 2026-09-27 追加：本地 dev 连接凭证隔离

`application-dev.yml` 改为环境变量和无凭证默认配置，本机账号密码放入 Git 忽略的 `server/application-dev.local.yml`，模板为 `server/application-dev.local.example.yml`。静态核对结果为 PASS；Maven 测试因本机仅有 Java 17.0.3、项目要求 Java 25 而于编译阶段 BLOCKED，应用启动以及 MySQL/Redis/MinIO 连接为 NOT_RUN。明细见 `../local-dev-credentials/README.md`。

## 2026-09-28：独立物品管理

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 物品独立域/生命周期/权限/幂等/成本单测 | PASS | 11项新后端单测；购买当天、闰日、零元、盈利、退役冻结、删除审计和重试。 |
| 真实DEV HTTP与数据库闭环 | PASS | V6已执行；Flyway/information_schema/Entity 22列一致；真实RSA登录、6并发重复创建、跨用户拒绝、退役/删除与记账零影响。隔离合成数据已清理。 |
| 前端回归/类型/双端构建 | PASS | 82/82、vue-tsc、H5及mp-weixin成功。 |
| H5只读视觉 | PASS | 320/375/414无横向溢出；列表、折线、表单失败保留和确认弹层；evidence中的PNG和JSON。 |
| 后端打包 | PASS | Java25 -DskipTests package；仅打包证据。 |
| 完整后端回归 | FAIL | 65项中63通过、1旧LoggingProfileConfigTest日志目录断言失败、1真实DEV测试默认跳过。真实DEV测试已显式另跑通过。 |
| 真实H5添加/退役 | PASS | 正式H5构建在隔离DEV会话下，从页面添加399.99元物品并以100元售价退役，详情成本299.99元、汇总归零及退役条目刷新；dev-ui.json与真实截图。临时服务/合成数据已清理。 |
| 微信工具/真机、真实H5删除按钮提交 | NOT_RUN | 微信仅构建；删除API与弹层取消已验证，真实页面删除未提交。 |
| 整体客户端闭环 | PARTIAL | 剩余微信工具/真机、真实H5删除按钮提交与旧日志测试问题。无本轮外部BLOCKED。 |

详见 [item-lifecycle](../item-lifecycle/README.md)。

## 2026-09-28：微信小程序启动白屏

`node-forge` 在微信服务上下文读取未定义全局对象导致 `app.js` 启动失败。定向构建转换后，开发者工具模拟器首页重新加载两次均可见；密码加密测试 2/2、前端回归 82/82、TypeScript 与双端构建通过。真机和真实密码提交仍为 NOT_RUN。详见 [wechat-mini-startup-crypto](../wechat-mini-startup-crypto/README.md)。

## 2026-09-28 追加：物品清单排序

物品清单查询按在役优先、购买日期由新到旧、ID倒序排序，分页前完成排序。静态代码和 API 契约已核对；测试、构建及 DEV/页面运行验收为 NOT_RUN，详见 `../item-list-order/README.md`。

## 2026-09-28 追加：物品详情资料编辑

物品详情新增编辑资料入口，可修改名称、购买价格和购买日期。后端使用当前用户锁、物品归属检查、输入校验和V7幂等关系表保护更新；保持状态与退役资料不变，详情成本使用新资料重算。静态核对、类型检查、H5/微信构建及Java 25跳过测试打包为PASS；DEV Migration/API与客户端运行验收待验证，详见 `../item-profile-edit/README.md`。

## 2026-09-29 追加：物品日期弹窗年份选择

物品日期范围调整为 `2000-01-01` 至当天，前后端校验与 API 文档一致。年份栏保留普通列表样式，只显示 2000 年至当前年的选项（2026 年时共 27 项），没有独立悬浮按钮或虚拟列表。浏览器中已观察到普通年份列表；实际点选和提交的浏览器交互状态以专项档案验证记录为准。

## 2026-09-29 追加：共用日期弹窗滚动修复

用户进一步明确年份可滚动范围为 2000–2099、默认当天，物品保存上限仍为当天。月/日首尾缺少留白使 9 月被中心算法读成 7 月、1 日被读成 3 日；共用日期/时间滚轮现统一首尾留白和坐标换算。修复前复现测试 0/2，修复后前端回归 87/87、类型检查与双端构建 PASS；H5 实际点选 8 月、27 日并确定后表单正确回写，拖动到 12 月和 2099 年后高亮不跳回，未来日期确认被弹窗拦截。未提交物品并已恢复当天默认值。微信工具验收为 NOT_RUN。详见 [专项档案](../date-picker-scroll-stability/README.md)。

## 2026-09-28 追加：底部导航前三项图标放大

首页、日历、资产图标使用与“我的”相同的 `scale(1.6)`；物品和我的图标样式保留。CSS 静态核对为 PASS；H5/微信运行视觉、类型检查及构建为 NOT_RUN。详见 [迭代档案](../bottom-nav-first-three-icons/README.md)。

## 2026-09-28 追加：五项导航图标统一为物品尺寸

此前字符图标的 1.6 倍缩放在 H5 中明显大于物品描边图标。本次移除额外缩放，并根据浏览器反馈细调各图标可见轮廓与首页、日历纵向位置；已更新 H5 底栏截图。类型检查和双端构建 PASS；微信开发者工具与真机视觉为 NOT_RUN。详见 [迭代档案](../bottom-nav-icon-size-unification/README.md)。

## 2026-09-29 追加：物品清单退役行灰底

物品行根据 `RETIRED` 状态显示灰色底色，在役行继续使用白色；不改动列表数据与交互。静态检查、类型检查及 H5/微信小程序构建 PASS；真实页面视觉为 NOT_RUN。详见 [迭代档案](../item-retired-row-background/README.md)。

## 2026-09-29 追加：页面跨端适配复查

H5 Chrome 与微信开发者工具模拟器逐页核对主导航及常用二级页面；修复公共页头、矮屏日历和横屏记账三处布局问题。前端 87/87、类型检查及双端构建 PASS；真机触摸、iOS Safari、全部有数据详情与业务写入 NOT_RUN。完整证据、尺寸和回退范围见 [responsive-pages-followup](../responsive-pages-followup/README.md)。

## 2026-09-30：待办清单

新增与账本无关的独立待办、V10 五表、按用户归属和幂等操作、重复发生项、到期提醒与逐次消息记录。DEV Flyway V10、`information_schema`/Entity、合成用户真实 HTTP/权限/生命周期、定向测试及前端双端构建已验证；Chrome H5 与微信开发者工具模拟器已实际点击测试。微信原生重复选值后的持久化 PARTIAL，真机 NOT_RUN，真实 Bark/pushplus 送达 BLOCKED。完整证据见 [ha-todo](../ha-todo/README.md)和[客户端测试报告](../ha-todo/11-client-test-report.md)。

同日补充独立新增/编辑路由、本人未完成详情读取、保存返回和按钮内容居中。H5/微信模拟器已执行增改删与返回流程；H5 刷新编辑页返回问题已修复并重测。详情见上述客户端测试报告。

## 2026-09-30 追加：验证后同步与自动提交

- requirement：用户要求每次代码写完且测试无误后，拉取远端、解决冲突并自动按中文 Conventional Commit 提交，写入长期规范。
- design：AGENTS.md 为权威入口，README、文档中心和测试规范同步引用；必需验证失败或阻塞时不自动提交，当次用户限制优先。
- database / api / backend / frontend：本次均无业务修改。
- testing：纯文档内容、Markdown 本地链接和 Git 空白检查；业务测试与构建不适用。
- commands：`git status --short` 初始无输出；upstream 查询为 origin/main；修改前与验证后 `git pull --ff-only` 均 Already up to date；Python 链接与规则入口核对 PASS；`git diff --check` 退出 0。
- verification：上述实际检查 PASS；真实冲突处理 NOT_RUN（没有冲突），后续提交结果以 Git 历史及交付报告为准。
- rollback：需要撤回时可 revert 本次规范提交，恢复此前项目规则。

## 2026-09-30 追加：自动提交包含推送

用户补充长期授权：验证、远端同步、冲突处理与复验通过后，自动提交并推送到当前分支 upstream，无需再次确认。AGENTS.md、项目 README、文档中心和测试规范同步更新；禁止强制推送，推送拒绝时重新同步复验，失败必须如实报告并保留本地提交。

本次为纯文档更新（database / api / backend / frontend 无变更），检查内容、链接与 Git 空白；实际同步及提交、推送结果以 Git 命令与交付报告为准，不据规则文字推断已推送。回滚可 revert 本次规范提交并正常推送。

## 2026-09-30｜本地后端端口 9898

PASS：开发后端默认端口及前端 development API 改为 9898；H5 仍为 5180，正式配置无差异。Java 25 打包、DEV 启动、认证公开接口 HTTP 200 与 localhost:5180 跨域预检通过。未进行业务写入；业务全量回归不适用。详情见 docs/10-iterations/2026/09/local-backend-port-9898/README.md。

## 2026-09-30｜物品日均成本图

完成长期跨度自适应非等距纵轴与刻度说明；9 项前端定向回归、类型和双端构建 PASS，375/320px 只读 H5 视觉 PASS。真实登录态 PARTIAL，微信模拟器/真机视觉 NOT_RUN；数据库/API/后端无修改。requirement、design、database、api、backend、frontend、testing、commands、verification、rollback 见 [本次完整迭代](../item-cost-chart-scale/README.md)。

## 2026-09-30｜待办自定义重复

requirement/design：采用截图重复业务逻辑，沿用现有样式；取消旧预设下拉，直接三模式。database/api/backend/frontend：V11与8个快照字段、CUSTOM契约、实际完成后起算、有限固定日期、多选和月末、双端统一组件。testing/commands/verification：定向后端13项、前端39项、类型/双端构建/打包、DEV9898启动及模拟器规则交互PASS；完整真实用户点击写入PARTIAL，真机NOT_RUN，实际通知BLOCKED。rollback：CUSTOM规则停用后回退应用，保留已执行V11。详情见[完整迭代](../todo-custom-repeat/README.md)。

## 2026-09-30｜待办表单反馈优化

落实八项反馈：标题/计划时间/重复必填星号，重复移除开关且默认按时间每1天，68px居中间隔输入，新建默认提醒开启，备注“选填”无括号，新建直接返回，编辑变更确认复用系统居中按钮，清单隐藏发送记录而数据库审计保留。PASS：41项前端回归、类型及H5/微信构建、实际H5/微信模拟器默认值与返回/确认交互。无数据库/API/后端变更，本轮未启动额外9898服务。真实用户保存与微信真机/全尺寸NOT_RUN（补充验收），外部送达仍BLOCKED。详情见[完整迭代](../todo-form-polish/README.md)。

## 2026-09-30｜待办列表与详情整改

PASS：47项前端定向回归、类型检查、H5/微信生产构建；Java25待办13项测试（无跳过，包含真实DEV HTTP+DB权限/历史详情/删除校验）与打包。PASS（H5隔离夹具）：375px圆圈中心偏差0px、暖黄期限与提醒角标、点击内容详情/修改规则/取消返回、圆圈完成刷新与历史详情仅删除、确认删除返回；320/414/812横屏无横向溢出。PASS（微信已测）：我的勾形、待完成/已完成列表、待完成详情与返回。PARTIAL：微信完成历史详情在9898旧服务报错，新接口已通过真实DEV随机端口HTTP验证，但9898未重启，模拟器未复测新接口。NOT_RUN（补充）：微信真机与真实用户完成/删除点击写入；真实用户数据未修改。外部通知送达仍BLOCKED（既有边界）。数据库无迁移。详见[迭代和截图](../todo-list-detail/README.md)。

## 2026-09-30｜待办提醒正文前缀与本地诊断

- PASS：Bark/pushplus 正文增加「哈记账： 」；Java 25 定向回归 14 项无失败/错误/跳过，验证 sender 正文与审计快照一致；后端打包通过。无数据库/接口字段/前端变更，前端构建不适用。
- PASS（前缀修改前诊断）：用户授权在本人 DEV 账号创建单次提醒，后台自动扫描并向 pushplus 发送，平台返回受理；此前 23:20 待办在 22:20 已完成，被扫描排除。
- FAIL（外部配置）：Bark 自动请求 HTTP 400，纠正前四次尝试失败；官方只读注册查询返回无法按当前保存值获取设备 Token。保存值为 64 位十六进制，用户确认误填设备 Token，需在通知中心改填 Bark 推送 Key；不回显敏感值。
- NOT_RUN（补充）：手机送达确认与新前缀终端验收；现有 9898 JVM 未重启，新代码需重启本地后端加载。此项不作为已送达证据。

详见 [本次迭代](../todo-reminder-prefix/README.md)。

2026-10-01 追加：用户授权后已将本人 DEV Bark 配置纠正为推送 Key，仍以 AES-GCM 加密保存并回读核对；官方只读注册查询 HTTP 200（PASS）。旧值失败记录保留。测试待办 Bark 待重试时间提前后，00:01:23 平台受理，投递为 SENT、最后一次尝试为 ACCEPTED；Bark/pushplus 双渠道平台受理 PASS，手机送达仍待用户确认。

## 2026-10-01｜待办编辑返回失效详情修复

修改规则会替换未完成发生项，原编辑页保存后回旧详情触发“待办不存在，请刷新”。现改为成功回清单并刷新，取消仍回原详情。PASS：先复现失败再修复的实际页面脚本回归、49项前端定向测试、类型检查、H5/微信构建。PARTIAL：页面脚本+API/导航夹具覆盖，未替代真实客户端。NOT_RUN（补充）：真实用户保存、微信工具/真机写入和部署。无后端、Schema、权限或金额实现改动；Java/数据库结构与视觉布局验收不适用。本次必需验证无FAIL/BLOCKED。

详见 [本次迭代](../../10/todo-edit-return/README.md)。

## 2026-10-01｜新增页面跨端适配

通知中心和待办清单/详情/新增按既有风格补齐窄屏换行、按钮居中、开关防压缩与详情固定底栏，两页漏接的统一微信转发已修复。PASS：108项回归、类型与两端构建，实际H5四种尺寸及微信430/320模拟器已测范围。PARTIAL：极限长文本/微信通知页/触摸键盘；NOT_RUN（补充）：真机、Safari和实际保存发送。无数据库/API/后端变化。完整requirement/design/database/api/backend/frontend/testing/commands/verification/rollback见[本次迭代](../../10/new-pages-responsive/README.md)。

## 2026-10-01｜待办标题高度修复

三个待办页漏传page-top-extra=20，导致微信标题较通知中心下移20px；已补齐。实际组件定位回归先失败后通过，110项测试、类型与双端构建PASS；模拟器画面PARTIAL，真机复验NOT_RUN（补充）。需求/设计/实现/验证及回退见[本轮完整迭代](../../10/todo-navigation-alignment/README.md)。

## 2026-10-01｜我的页面切回闪烁

修复每次onShow将状态文案替换为加载提示、头像先回默认再加载的问题。同会话保留资料并后台刷新，复用未过期头像地址，处理更新/移除、失败与旧响应隔离。PASS：8项定向回归、118项全量、类型和H5/微信构建；PARTIAL：脚本与模板隔离验收；NOT_RUN（补充）：真实用户页面切换、微信工具/真机及部署。database/api/backend无修改。完整requirement/design/frontend/testing/commands/verification/rollback见[本次迭代](../../10/mine-profile-refresh/README.md)。

## 2026-10-01｜四个主导航页面刷新闪烁

首页/日历/资产/物品同会话同条件切回时保留原内容并后台刷新，空状态同样保留；首次及月份/日期/筛选切换仍有反馈，后台失败toast告知，响应序号与会话清理防覆盖。PASS：22项专项、140项全量、类型与双端构建；PARTIAL：实际脚本和模板隔离验收；NOT_RUN（补充）：真实用户切换、微信工具/真机和部署。database/api/backend无变更，完整requirement/design/frontend/testing/commands/verification/rollback见[本轮迭代](../../10/tab-background-refresh/README.md)。

## 2026-10-01｜帮助页移除版本文案

- requirement/design/frontend：按用户要求，将帮助页底部“哈记账 · v1.0”改为“哈记账”，同步移除分隔点，保留品牌图标和标语。
- database/api/backend：无变更，相关验证不适用。
- testing/commands：`node --test app/tests/*.test.mjs` 140/140 PASS；`pnpm --dir app run typecheck`、`pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin` 均退出0（PASS）。Python断言核对帮助页源码和微信构建WXML不含v1.0（PASS）。
- verification：上述源码、回归和构建检查PASS；真实客户端画面验收NOT_RUN（补充），无部署操作。
- rollback：将帮助页底部品牌文字恢复为原文案。

## 2026-10-01｜记账日期、滚动与键盘振动

PASS：145项全量回归、类型检查、H5/微信生产构建；本机 H5 新增/普通编辑/还款编辑触摸滚动，键盘固定；微信日期/时间选择二级与一级确认回填；实际 H5 按键振动探针 [15,15] 已恢复。修复 H5 禁滚监听误拦截、微信滚轮祖先拦截与一级弹窗 `.self` 误关、键盘 DOM 依赖。

PARTIAL：硬件震感和所有共享组件调用页；BLOCKED（补充）：本机没有连接可用 Android 真机，手机浏览器尚未确认，微信真机触摸/震感未验；NOT_RUN（补充）：部署、真实账单保存。无必需代码验证 FAIL；无数据库/API/后端变更。

见[完整迭代](../../10/entry-touch-interactions/README.md)。


## 2026-10-01｜物品日期点击误关表单

PASS：本机微信工具新增、编辑、退役日期打开、选择、确认回填，一级表单保留；H5生产只读夹具同三入口与背景/关闭按钮取消；146项回归、类型、双端构建及微信WXML事件边界。CenterModal的祖先背景 `.self` 在微信内部点击时误触发关闭，改为独立遮罩与内容兄弟节点，仅H5内容停止触摸冒泡。

PARTIAL：尚无微信手机真机触摸证据；NOT_RUN（补充）：真机、发布及真实物品保存；无数据/API/后端修改，测试草稿均取消。见[完整迭代](../../10/item-date-modal-click/README.md)。


## 2026-10-02｜新增与编辑记账跨端滚动

PASS：149项前端回归、类型、H5/微信生产构建、微信编译原生 scroll-view / 无主体 catchtouchmove / 无 disableScroll；工具主体滚轮与账户选择/取消。H5生产只读夹具覆盖4个视口及新增/支出/转账/还款编辑，上滑/回滑、备注可达与键盘固定（高屏无溢出时无需滚动）。

PARTIAL：代码修复完成；原 H5 本机仿真也能滑动，未复现全部手机浏览器触发条件。BLOCKED：无可操作真机，手机 Safari/Chrome 和微信手势验收未完成；真机验收状态保持 BLOCKED；用户在了解该限制后明确要求提交代码，本轮按当次指令提交并推送，不将真机状态改为 PASS。NOT_RUN：部署/发布与真实账单保存。见[本轮记录](../../10/entry-native-scroll/README.md)。


## 2026-10-02｜微信全局转发修复

PASS：实际uni-app原生分享生命周期初始化回归、全量130项测试、类型、H5/微信生产构建；微信工具首页/我的/帮助转发入口可用，首页与我的确认卡片标题和固定品牌图正确。工具回调间接注册未生成原生分享方法，改为全局mixin，自动覆盖全部页面；固定首页与品牌图避免携带个人数据与默认截图。H5不注册微信mixin。

PARTIAL：工具只手动抽查3页；NOT_RUN（补充）：真机发送、好友接收打开及线上发布版本。朋友圈当前页分享未开放；无数据库/API/后端变更，无上传发布操作。完整需求、设计、测试、命令、验证和回退见[本轮迭代](../../10/wechat-global-share/README.md)。


## 2026-10-02｜固定启动页分享文案图

PASS：用户确认的500×400文案PNG已接入全局分享，约17KB，无图标、插画、账单、余额或当前页面截图；微信工具实际分享框显示正确。132项回归、类型、H5/微信最终构建与打包图片一致性通过。页面加载时将固定图复制到微信本地文件目录，避免工具分享框代码包路径破图；复制失败也指定固定图片。

PARTIAL：工具抽查首页分享卡片。NOT_RUN（补充）：手机发送与接收、上传发布；没有消息发送或用户数据变更。无后端/数据库/API修改。见[本轮记录](../../10/wechat-welcome-card/README.md)。


## 2026-10-03｜微信记账备注输入

PASS：134项回归、类型、H5/微信构建、微信DEV编译及两套WXML/WXSS边界；微信工具AX操作新增备注输入保留弹窗、6/100计数、完成回填、重开和取消保留；页面函数覆盖新增空备注及编辑已有备注。备注内容祖先关闭与触摸拦截改为独立兄弟遮罩，内容定位在遮罩上方。

PARTIAL：工具AX未完整复现原始误关症状，根因由模板事件路径推断；真实编辑UI与手机键盘NOT_RUN（补充）。BLOCKED（补充）：原生坐标操作noWindowsAvailable、H5浏览器权限被拒绝。真实账单保存及发布NOT_RUN；后端/数据库/API无变更。详见[本轮迭代](../../10/entry-note-input/README.md)。


## 2026-10-03｜微信顶部固定

requirement/design/frontend：顶部标语、标题和返回按钮固定视口，等高占位保留胶囊对齐与首段位置。database/api/backend：无变化、不适用。testing/commands/verification：类型、134项回归、H5/微信构建及24组真实组件隔离滚动PASS；微信页面显示PARTIAL；工具自动滚动窗口失败及真机不可得BLOCKED。未提交推送、未部署发布。rollback：恢复公共组件原实现。完整记录见[本轮迭代](../../10/wechat-fixed-navigation/README.md)。


## 2026-10-03｜日历主体整体滚动

PASS：134项前端回归、类型检查、H5/微信生产构建、微信WXML唯一原生scroll-view且无catchtouchmove。H5生产只读夹具320×568、375×667、430×932、667×375触摸上滑，月历随主体移动，标题和底栏固定，第12笔完整可见且距底栏25px；五行/六行月历、日期选择、空态、失败重试及月份弹窗通过。取消账单独立高度与740px分支。

PARTIAL：微信生产产物已验证。BLOCKED：微信工具原生窗口操作返回noWindowsAvailable，AX操作后截图与焦点状态未能证明实际滚动；必需微信平台滚动验收未完成，按AGENTS.md暂不提交/推送。NOT_RUN（补充）：手机微信/iOS Safari、部署与发布；无数据库/API/后端及真实账务写入。见[完整迭代](../../10/calendar-content-scroll/README.md)。

2026-10-03 电脑控制复测：实际点击进入我的/帮助页；重绑进程、Raise及重置会话后，滚轮仍报noWindowsAvailable，键盘输入无可观察效果。实际微信滚动验收继续BLOCKED，未提交推送。


## 2026-10-03｜用户明确要求提交当前改动

用户本次明确要求“帮我提交代码”，将微信顶部导航固定与日历主体滚动一并纳入提交范围；此前“暂不自动提交”的记录保留为历史状态，本次按明确提交指令执行。微信实际手势验收仍为 BLOCKED，未据此宣称平台运行验收完成。

提交前复验 PASS：`node --test app/tests/*.test.mjs`（134/134）、`pnpm --dir app run typecheck`、`pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin`；设置本机运行库和 Chrome 路径后执行 `node app/tests/fixed-navigation-browser.cjs`（24/24）与 `node app/tests/calendar-scroll-browser.cjs`（4/4视口）。微信生产 WXML 检查：唯一 scroll-view、无 catchtouchmove。浏览器证据使用只读夹具，无真实账务写入；无后端、数据库或 API 变更，未部署或发布。

同步检查 PASS：包含未跟踪文件的 stash 安全备份后，`git pull --ff-only` 返回 Already up to date；恢复后16个文件 SHA-256 全部一致，确认无代码或依赖变化后删除本次 stash。构建与浏览器运行证据对应同一份代码；同步后再次检查回归、类型和差异。


## 2026-10-03｜微信统一10px间距

requirement/design/frontend：全部微信顶栏与正文统一10px，真实首屏坐标校正安全区，补齐品牌及物品详情顶部补偿。database/api/backend无变化。testing/commands/verification：134项回归、类型、双端构建、本机64组组件布局及微信首页/资产初始抽查PASS；其余页面手动抽查PARTIAL，真机与发布NOT_RUN（补充）。rollback恢复公共组件、品牌头及物品详情原布局。详见[记录](../../10/wechat-navigation-spacing/README.md)。

## 2026-10-03｜滚动后导航大空隙修复

PASS：移除共享导航一次性异步坐标补偿，以同一CSS安全区抵消页面padding；修复前安全区变化时多出29px的失败用例，修复后104组实际调用配置布局、134项前端回归、类型检查、H5/微信构建、H5日历4视口和记账16组触摸回归通过。微信iPhone 5模拟器日历、物品、新增记账实际滚动及回顶截图验证通过，页头、底栏和键盘保持固定，回顶恢复10px初始间距。

PARTIAL：未在微信工具手动逐页检查全部配置，其余调用配置已由104组组件运行回归覆盖。NOT_RUN（补充）：手机微信、iOS Safari、部署发布；数据库/API/后端及真实账务写入不适用。本轮无必需FAIL/BLOCKED；CLI服务端口关闭与早期窗口控制错误已通过原生工具界面复测替代，无安全设置变更。详见[迭代与截图](../../10/navigation-scroll-gap/README.md)。


## 2026-10-03｜体验版分享回调注册整改

PASS：20个路由页面改为显式分享选项，移除依赖App初始化顺序的全局mixin；实际uni-app初始化复现App未就绪时全局回调缺失并缓存，修复前21项失败、修复后24项分享回归及154项全量回归通过。类型检查、H5/微信生产构建、20页生产选项原生回调检查及PNG一致性通过；微信工具首页分享框标题与固定欢迎文案图正确，无财务截图，取消未发送。

PARTIAL：工具只手动抽查首页；NOT_RUN（补充）：手机体验版新包复测与好友接收打开，原手机现象的唯一根因未取得调用栈证明。未上传或发布。完整需求、设计、命令及回退见[本轮记录](../../10/wechat-experience-share/README.md)。


## 2026-10-03｜分享欢迎图改用服务器资源

PASS：复用staticResource的公开HTTPS图片，远端HTTP200、image/png、500×400、17580字节，与原图内容相同；删除微信本地复制，原图及渲染输出移到offloaded-static-assets/wx，不再打包。23项分享及153项完整前端回归、类型、H5/微信构建和20页实际生产回调检查通过；工具首页分享框远端欢迎图与标题正常显示，取消未发送。

PARTIAL：工具仅手动抽查首页。NOT_RUN（补充）：手机新包图片加载、好友接收打开；未上传发布或改变服务器权限。完整记录见[本轮迭代](../../10/wechat-share-remote-image/README.md)。


## 2026-10-04｜微信发现新版本后强制更新

PASS：新增全局更新守卫及专用更新页，微信报告新包后阻止继续使用旧界面，下载完成仅允许重启更新；失败/超时保留拦截并提示关闭重开，不清业务缓存。11项定向、165项前端回归、类型、H5/微信构建、实际生产包接线/H5排除及21页分享回归通过；微信工具官方更新模拟成功重启回首页、失败确认后仍拦截、原生跳页被拦回，无业务写入。

PARTIAL：只实测一款工具模拟器。NOT_RUN（补充）：手机正式版跨版本升级及上传发布；需先上线含本功能的版本，不能追溯执行尚未包含本逻辑的旧包，也不能保证微信报告更新前或离线时一定最新版。无数据库/API/后端变更；分享问题不据此宣称修复。完整证据见[本轮迭代](../../10/wechat-required-update/README.md)。


## 2026-10-04｜账单与账户逻辑删除修复

PASS：真实MyBatis SQL复现markDeleted + updateById排除deleted字段；账单和账户改显式deleted=1、用户/未删除条件与删除/更新审计，并检查影响行数。Java25全量测试与打包99项中95通过、4条件跳过，本次DEV MySQL5项全通过，覆盖四类账单、关联退款、重复删除/他人拒绝、余额恢复、删除查询剔除和零行更新事务回滚。过时Windows日志测试断言已同步到现有DEV配置。退款、物品、文件、通知配置、待办未发现同类SQL写法缺陷。

PARTIAL：DEV身份为夹具，其他入口是SQL核查，未替代全部HTTP/MinIO验收。NOT_RUN（补充）：微信真机、部署与历史受影响余额逐笔核对；只读审计SQL已提供，不自动修补旧数据。无前端/Schema变更。本轮必需验证无FAIL/BLOCKED，详见[完整迭代](../../10/logical-delete-fix/README.md)。


## 2026-10-04｜日历月度汇总与当天收支

PASS：日历卡片显示所选月支出/收入/结余，单日“收 / 支”位于笔数左侧；165/165前端回归、类型检查、H5/微信构建与4视口H5只读夹具运行验证通过，覆盖切日不改月汇总、切月更新、空日、失败重试和长金额无横向溢出。PARTIAL：运行证据限于H5夹具；NOT_RUN（补充）：微信真机、真实账号、部署/发布。无后端/数据库/API契约变更。见[完整记录](../../10/calendar-month-summary/README.md)。


## 2026-10-04｜物品与账户详情修复

PASS：账户金额以无千分位字符串回填，资金余额/信贷欠款保留负值；编辑弹层防止点击冒泡、保存后更新共享账户缓存。账户流水支持50笔分页、总数、触底/按钮加载、错误重试与筛选响应隔离；物品图表改普通view绘制并预留固定栏安全区。169/169前端回归、类型检查、H5/微信构建、4视口12个生产H5夹具场景通过，验证123笔跨年流水与62笔筛选流水，横屏不遮挡重试按钮；微信产物无canvas并包含分页事件。

PARTIAL：运行证据为H5合成接口夹具；NOT_RUN（补充）：微信工具/真机、真实账户接口验收、部署/发布。无后端/数据库/API契约变更，本轮不重证账务事务和权限。见[完整迭代](../../10/item-account-detail-fixes/README.md)。


## 2026-10-12 追加：Android 微信全局金额格式化

共享金额展示脱离 Intl/locale 参数依赖，整数隐藏 .00，非整数固定两位；截图长小数及图表刻度由实际组件与微信生产包模拟渲染回归覆盖。173 项前端回归、类型和双端构建 PASS；Android/iOS 手机新包与上传发布 NOT_RUN。详见[本轮档案](../../10/android-number-format/README.md)。


## 2026-10-15｜代码 Review 前置规则

- requirement/design：每次代码完成后先 Review，发现问题先修复并复审，确认无未解决问题后再测试，验证通过后才提交推送；后续代码变化与远端同步后的最终代码同样遵循该顺序，并记录 Review 范围、结论和修复情况。
- database/api/backend/frontend：无业务实现变更；更新 AGENTS.md、项目 README 与测试规范，审计记录同步追加。
- testing/commands：已执行 `git diff -- AGENTS.md README.md docs/07-testing/README.md` 审阅，本次差异符合需求，无未解决问题；Python 检查三个当前规范文件的本地 Markdown 链接及流程顺序断言通过，`git diff --check` 退出 0。
- verification：上述文档审阅、内容与链接检查 PASS；业务测试、构建和 DEV 运行因纯文档修改不适用，无本轮 PARTIAL、FAIL 或 BLOCKED。
- rollback：回退本次文档提交，恢复原工作流表述。


## 2026-10-15 追加｜Review 仅限本次任务改动

- requirement/design：按用户补充，Review 及后续复审仅针对本次任务新增或修改的代码、相关冲突解决差异；允许读取上下文判断影响，不扩大到无关代码。
- database/api/backend/frontend：无业务变更；同步 AGENTS.md、项目 README、测试规范和审计记录。
- testing/commands：审阅 `git diff -- AGENTS.md README.md docs/07-testing/README.md`，修正文档语句后确认无未解决问题；Python 范围断言及当前规范本地链接检查通过，`git diff --check` 退出 0。
- verification：文档审阅、内容及链接检查 PASS；业务测试、构建与 DEV 运行不适用，无本轮 PARTIAL、FAIL 或 BLOCKED。
- rollback：回退本次文档提交。


## 2026-10-15 追加｜Jenkins 凭证与自动发布规则（历史授权已撤销）

- requirement/design：长期授权 Review、测试、同步、提交与推送通过后按改动发布后端 `haji-server` / 每次前端 `haji-h5`，两端同时改先后端再 H5；微信上传/提审/发布由用户处理。凭证仅存本地忽略文件。
- database/api/backend/frontend：无业务、数据库或 Pipeline 修改；更新 AGENTS.md、README、运维/测试规范、示例及忽略规则，无需执行 SQL。
- testing/commands：本轮差异 Review 无未解决问题；Python 文档内容/链接/锚点、JSON、Git 忽略与权限检查通过，`git diff --check` 退出 0；系统 curl 查询文件夹 API HTTP 200，确认两个任务存在。
- verification：规则及本地文件检查 PASS；后续鉴权验收因本地用户名/API Token 未填为 BLOCKED，真实发布 NOT_RUN，本轮纯规则配置不发布。业务测试、构建和 DEV 联调不适用。
- rollback：回退本轮文档及示例提交；真实 Token 在 Jenkins 撤销后再删除本地文件。详见[本轮档案](../../10/jenkins-auto-release/README.md)。


## 2026-10-15 追加｜Jenkins 真实发布验收（历史记录）

- requirement/design：用户本地填写 Key 后授权实测发布；进程内读取凭证，以 `BRANCH=main` 顺序发布后端、检查公开接口，再发布 H5。
- database/api/backend/frontend：无实现、Schema、Pipeline 或配置修改；目标 `9c58af6` 与上次构建的后端/前端业务内容一致，无新增迁移，无需手工执行 SQL。
- testing/commands：身份认证成功，GitHub/Gitee/本地 main 一致。队列 113 → haji-server #34 SUCCESS、部署目录更新到目标修订、容器运行，公开接口 HTTP 200/code=0；队列 115 → haji-h5 #28 SUCCESS，类型/编译/Nginx 配置检查和线上 HTML/JS/CSS 探针通过；凭证仍受 Git 忽略且权限 700/600。
- verification：上述真实发布与公开探针 PASS，原鉴权 BLOCKED 已解除，本轮无 FAIL/BLOCKED。PARTIAL：未做真实账号业务、数据库/MinIO 全链路与浏览器视觉验收；小程序工具/真机和发布 NOT_RUN。
- rollback：采用已验证版本重新构建，不自动回滚 Schema。完整构建链接见[本轮档案](../../10/jenkins-auto-release/README.md#2026-10-15-追加真实凭证与发布验收)。本轮后续仅提交证据文档，不再次发布。


## 2026-10-15 追加｜撤销自动发布与清理凭证

- requirement/design：按用户最新决定撤销代理的 Jenkins 后端/H5 自动发布及主动 Gitee 同步；后续所有平台发版均由用户操作，保留原有 Review、测试与 GitHub 提交推送规则。
- database/api/backend/frontend：无业务、数据库或生产配置变更，无需执行 SQL；原有 Jenkinsfile 与线上任务保持不变。
- testing/commands：仅审阅本次撤销/删除及记录差异，按纯文档范围验证内容、本地链接、删除状态与 Git 空白；结果在[本轮档案](../../10/manual-release-only/README.md)记录。
- verification：本地凭证/模板与暂停的同步草稿已删除，旧发布授权明确失效；本轮不触发发布或主动同步镜像，不改远端账户令牌。
- rollback：后续若再次需要代理发版，须重新明确授权并配置凭证；不恢复被删除的真实 Key。


## 2026-10-17 追加｜Gitee 本机直推

- requirement/design：取消 GitHub → Gitee Pull 镜像；保留 GitHub 拉取，本机 origin 配置双端直接推送，gitee remote 用于核对。
- database/api/backend/frontend：无实现或数据库变化，无需 SQL，发版仍由用户处理。
- testing/commands/verification：Gitee 镜像空列表、GitHub 对应 Webhook Active=0、Gitee 无 Webhook、本机 remote/pushurl 和远端祖先关系已核对；本机双推成功且两端 main 与本地 d97e218 一致，文档链接/锚点、配置与空白检查 PASS；最终复验见[本轮档案](../../10/gitee-direct-push/README.md)。业务测试/构建/DEV 不适用。
- rollback：移除本机新增的 origin pushurl 及 gitee remote 可恢复 GitHub 单推；恢复镜像前需重新确认意图与两端提交。


## 2026-10-18｜每次修改后说明更新范围

- requirement/design：AGENTS.md 新增最终汇报要求，逐项说明后端、H5、微信小程序及其他受影响服务是否需要更新和原因；涉及 Migration、SQL、配置或静态资源时说明操作、实际执行状态与必要顺序。无需部署时明确告知，无自动发布授权变化。
- database/api/backend/frontend：纯文档修改，无业务、数据库或配置变化；本次无需更新任何服务或客户端，无需执行 SQL。
- testing/commands：Review 仅覆盖本次新增规则与本条审计记录，核对需求和用户自行发版边界，无未解决问题；Python 内容断言通过，新增文本无 Markdown 链接，`git diff --check` 退出 0。
- verification：规则内容与 Git 空白检查 PASS；业务测试、构建及 DEV 运行因纯文档修改不适用，无本轮 PARTIAL、FAIL 或 BLOCKED。
- rollback：回退本次文档提交。
- 同步：`git pull --ff-only` 返回 Already up to date；抓取并核对 GitHub/Gitee main 均与本地 HEAD 一致。stash 恢复后四份文档 SHA-256 一致，确认后删除备份；同步后复审仍无未解决问题。

## 2026-10-19｜手机还款账户列表滚动

独立遮罩替代滚动祖先的触摸取消，账户选择使用原生 scroll-view；面板高度限制让横屏返回按钮保持可见。Review 仅覆盖本轮页面、样式、两个测试文件及文档，无未解决问题。PASS：175/175 前端回归、TypeScript、H5/微信构建、四手机视口生产 H5 上下触摸滑动/末项选择/金额保留/背景关闭及微信 WXML。微信开发者工具已有项目通过 UI 验证滚轮滚至末项、选择并取消表单，未提交还款；CLI 服务端口关闭，采用 UI 替代验证，未改安全设置。

PARTIAL：H5 为合成接口，工具滚轮不等于手机手势；NOT_RUN（补充）：手机微信、Safari/Chrome 真机触摸和发布。无未解决必需 FAIL/BLOCKED。后端无需更新；H5 静态产物及微信小程序新包需由用户发布，无先后依赖；无数据库、SQL、配置或额外图片资源操作。详细命令、Review 和边界见[本轮档案](../../10/repayment-picker-scroll/README.md)。

## 2026-10-19｜微信还款金额点击误关闭

微信工具复现点击金额框后弹窗消失，定位主遮罩 click.self 编译后未保留过滤。主表单改独立同级遮罩，输入与面板祖先无关闭事件，账户选择回填和金额规则不变。PASS：仅本轮差异 Review、176 项前端回归、类型检查、H5/微信构建、生产 H5 四视口；微信模拟器实际金额编辑为 12.34、账户选择后保留、取消和背景关闭，未提交还款。PARTIAL：桌面模拟器/H5 合成接口；NOT_RUN（补充）：手机软键盘及发布后验收，无未解决必需 FAIL/BLOCKED。后端无需更新；微信小程序新包及 H5 静态构建由用户发布，无先后依赖，无数据库、SQL、配置或额外图片操作，本轮未发布。详见[本轮档案](../../10/repayment-amount-input/README.md)。

## 2026-10-19｜微信退款金额输入误关闭

退款输入及面板祖先移除关闭事件，独立同级遮罩保留提交中禁止关闭，金额/幂等业务不变。本轮 Review 仅覆盖修改页面、样式、2 个新增回归与必要文档，无未解决新增问题。PASS：178 项前端回归、类型检查、H5/微信构建、生产 H5 支出/收入四视口共 8 场景（输入、关闭、超额、禁用、失败保留/幂等重试及刷新）、微信产物和模拟器实际修改金额为 12.34，未提交真实退款。PARTIAL：模拟器/合成接口；NOT_RUN（补充）：手机软键盘及发布。后端无需更新，微信新包与 H5 静态构建需用户发布，无先后依赖，无数据库、SQL、配置或额外图片操作，本轮未发布。

全量 36 个 SFC/16 个 scroll-view 复查：当前微信可达剩余 6 处同类历史风险（改密、删除账单/退款、删除账户、记账放弃、待办放弃、待办删除），本轮未修改；源码及 WXML 确认，删除账单标题为上轮运行复现，其余实际交互待验。详细命令与清单见[本轮档案](../../10/refund-amount-input/README.md)。

## 2026-10-24｜H5 主屏幕应用图标

线上主屏幕图标旧路径 HTTP 404，H5 入口改为引用仓库已有白底品牌图，由 Vite 生成带 /haji/ 前缀和内容哈希的 PNG，避免重新打入微信 static。仅 Review 本轮 HTML、生产产物回归脚本和必要文档，无未解决新增问题。PASS：旧产物回归先失败、修复后 favicon/主屏幕图 HTTP 200 image/png 且与源图一致；全量前端回归、类型检查、H5/微信构建、DEV 实际页面/图片 HTTP 探针、小程序未包含白底图检查。PARTIAL：资源链路验证不替代 iPhone 系统 UI；NOT_RUN（补充）：发布后手机 Safari 删除旧入口并重新添加验收。本轮无未解决必需 FAIL/BLOCKED。

仅 H5 需要由用户发布完整静态产物（含新哈希 PNG），后端和微信小程序无需更新，无数据库/SQL/环境配置操作，无发布顺序依赖；本轮未发布。命令、Review 与回滚见[本轮档案](../../10/h5-home-screen-icon/README.md)。

## 2026-10-25｜iPhone Safari 金额键盘轻触反馈

requirement/design/frontend：仅 H5 金额键盘增加直接触摸原生 switch 的系统短触感适配；保留原按钮可访问语义与单次保存入口，微信按键条件编译不变。database/api/backend 无变化，不涉及 SQL、认证、事务或幂等修改。testing/commands/verification：本轮差异 Review 及修正复审、180/180 前端回归、TypeScript、H5/微信构建、Chromium/WebKit 6 个手机视口与 2 个回退场景、原有 16 组滚动回归、DEV WebKit 触摸与空格输入、微信排除适配检查 PASS。PARTIAL：桌面引擎/只读夹具不能证明实际震感；NOT_RUN（发布后补充）：iPhone Safari 物理触感与 VoiceOver 真机验收，无未解决必需 FAIL/BLOCKED；详见[本轮档案](../../10/ios-calculator-haptics/README.md)。rollback：回退本轮接线、组件、回归与文档后重建 H5。仅 H5 需用户更新静态产物，后端/微信无需更新，本轮不发布。


## 2026-10-25 本机部署与 Jenkins 退役

本轮变更及实际验证状态见[任务记录](../../10/local-docker-deploy/README.md)；取代 Jenkins 和 Gitee 双推规则，历史记录不再作为当前操作指令。


## 2026-10-25 修改后自动发布且不备份旧版本

当前规则及本轮验证证据见[任务记录](../../10/auto-release-no-backup/README.md)；取代先前后端/H5 由用户自行发版规则。微信小程序仍独立发布，旧迭代仅作历史追溯。


发布实测：PASS：实现 `84bf371` 已推送核对，`release.py all` 退出 0，后端/H5 均发布成功（2026-10-25 20:29:20 北京时间）；容器 running/0 重启且 revision 一致，公网 API 401，H5 HTML 及 5 个入口资源内容一致。旧后端镜像已删除，无旧版本备份/网页暂存/上传残留。10 项部署安全回归、Java 25 verify（90 执行通过、6 默认跳过）、H5 类型检查/构建 PASS。PARTIAL：公开探针不替代真实账号业务全链路；无未解决必需 FAIL/BLOCKED。微信无源码变化，无需更新且未发布；无 Migration/SQL 操作，其他服务未发布。发布后纯文档证据不再次部署。


## 2026-10-30｜独立彩票功能

实现：用户表lottery_enabled默认0，不设角色；我的页待办下方按权限显示入口。DLT/SSQ官方数据本地持久化，个人单次/计划/批次/逐期快照独立于账本；即时本地兑奖、单次结果/批次到期及官方更正事件，复用Bark/pushplus。界面沿用现有薄荷绿组件。

|验收项|状态|证据与边界|
|---|---|---|
|任务新增/修改代码两轴Review及问题复审|PASS|仅本次改动；修复编辑结算、分页链路、UNKNOWN锁、事务、同步/投递旧租约与更正去重|
|Java25完整verify|PASS|107项：100执行通过，7条件跳过，JAR生成成功|
|真实DEV/Flyway/字段与Entity/权限/官方接口/兑奖/提醒状态机|PASS|V12；合成账号，官方真实数据；投递发送使用替身，仅证明状态机|
|前端回归/类型/H5及微信构建|PASS|相关24项回归；类型及两端构建退出0|
|真实H5页面和320/375/414px|PASS|入口、保存即时结算、删除弹层、20期批次、SSQ本地历史与奖金；截图见彩票iteration/evidence|
|微信开发者工具模拟器|BLOCKED|应用已启动，但原生自动控制两次30秒超时；等待工具窗口/授权状态确认|
|真实通知渠道受理|BLOCKED|尚缺用户指定的测试接收账号；不使用替身冒充真实受理|
|微信真机|NOT_RUN|补充平台验收，不等同于构建或模拟器|
|Git提交/推送/自动发布|BLOCKED|用户附件明确必需验收BLOCKED时不能跳过门槛；代码保留工作区|
|生产V12迁移|NOT_RUN|待验收后先审核/备份/正式Flyway，核对摘要再发布|

后端、H5及微信小程序均需要更新；当前均未发布，生产原版本保留。微信由用户上传发布。未给真实账号开通权限。原有业务数据、密钥、配置及备份不变。新需求和当前证据详见[彩票迭代](../../10/lottery/README.md)。


## 2026-10-30｜用户验收范围更新及真实推送

用户本轮明确“微信小程序不验证，只验证h5”，因此微信模拟器/真机运行验证从本轮必需门槛排除，历史两次连接超时记录保留，不再阻塞交付；已完成的小程序构建不等同运行验证，上传发布仍由用户负责。

PASS：DEV仅admin账号配置了Bark/pushplus。按用户“现在”的回复及已告知的接收对象，显式运行LotteryLiveNotificationDevTest（lottery.live.verify=true、lottery.live.account=admin）；两渠道各发送一条“彩票功能验收”并均返回ACCEPTED，前后lottery_enabled未变。测试隔离TodoReminderWorker，LotteryWorker关闭，未触发无关提醒。仅证明渠道受理，不宣称设备送达。

新增实际命令：Java25 mvn -f server/pom.xml -Dtest=LotteryLiveNotificationDevTest -Dlottery.live.verify=true -Dlottery.live.account=admin test，退出0。测试Key仅在内存解密，不进入命令或文档。

本轮必需功能验证与H5验收已通过，无剩余必需FAIL/BLOCKED。后续按既定流程安全保存、pull --ff-only、恢复核对、同步后复审复验，再提交推送，先正式生产V12迁移，再发布后端/H5。此处尚不宣称提交/发布已完成。


## 最终交付（2026-10-30 13:43，北京时间）

PASS：实现提交 `ba1bed0 feat(lottery): 新增独立彩票记录与定投提醒` 已普通推送 GitHub origin/main，核对远端等于本地。按用户最新要求只验H5，不执行微信模拟器/真机运行验收或上传发布。

生产正式V12迁移PASS：迁移前haji为V11，数据库单事务压缩备份及完整性核对成功；首次入口独立validate将待执行V12判为错误，DDL前安全停止，复查仍V11/无彩票表。修正为migrate(validateOnMigrate=true)内部强制校验并经复审后重试成功，不添加ignore规则。最终V12成功、10张表、9个Entity字段集/注释、权限默认0及真实账号开通数0均核对。迁移摘要 `2befcc2a90f9e19909e89f55cc1f54035c1726664ce810b7aec1cb5d38235980` 在全部检查成功后更新，未绕过发布门槛。两份数据库备份及迁移日志/收据保留于服务器私有db-backups；未创建旧应用版本备份。

发布：`python3 deploy/local/release.py all` 退出0；先后端健康检查通过，再更新H5。

|组件|需要更新|实际结果|版本/依据|
|---|---|---|---|
|后端|是，新增API、权限、同步和兑奖任务|PASS，已发布|容器running、0重启，OCI revision=ba1bed06e7453b825553256250de3bc4b78855aa；backend-release.txt时间13:43:06+08:00|
|H5|是，新增彩票页面及入口|PASS，已发布|h5-release.txt时间13:43:07+08:00；HTML、5个彩票JS模块及5个入口资源逐字节匹配本机构建|
|微信小程序|使用新彩票入口需要新版客户端|本轮未运行验收、未上传/提审/发布|按用户指令排除；此前构建通过，后续用户自行发版|
|数据库|是，先V12再应用|PASS，正式Flyway已完成|生产V12 success=1，全部实际用户lottery_enabled=0|

运行验收PASS：生产自动同步DLT30期（最新26123，13:43:25.766成功）、SSQ30期（最新2026124，13:43:26.341成功），last_error均为空，历史回填游标2；后续后台继续增量/历史补齐。不能据此宣称全部历史已回填。彩票公开接口未登录返回HTTP401/code1。仅剩当前后端镜像，无ledger-upload/ledger-migrate临时目录。原数据库、Redis、MinIO、运行密钥、文件、日志和数据备份保留。

真实通知PASS：BARK和PUSHPLUS各一条“彩票功能验收”均ACCEPTED，既有admin权限未改变；不宣称设备送达。默认所有真实用户无入口，维护者后续按用户ID设置app_user.lottery_enabled=1才能使用。

当前无未解决必需FAIL/BLOCKED。边界：DLT早于19019及未知派奖按设计待核实；完整历史渐进回填。微信运行按用户明确指令不适用，不将未执行写成PASS。最终纯文档证据提交不触发重复发布。

发布入口Java25 verify实际统计：108总/95执行通过/13条件跳过，0失败/0错误；发布构建不重复发送真实提醒。


## 权限补强最终发布（2026-10-30 13:58，北京时间）

PASS：`6980b29 fix(lottery): 禁止业务写入回写彩票权限` 已普通推送GitHub origin/main并核对一致。`python3 deploy/local/release.py backend`退出0，仅后端更新；H5无源码变化，保持原已发布版本。

|组件|最终运行版本|实际验证|
|---|---|---|
|后端|6980b2956a35f594187327d42ae36ae1f58e8650|13:58:02+08:00发布；running/0重启，OCI revision及backend-release.txt一致，公开彩票接口401/code1|
|H5|ba1bed06e7453b825553256250de3bc4b78855aa|保持13:43:07+08:00成功发布版本，无需为纯后端补强重复更新|
|生产数据库|Flyway V12 success=1|无新增Migration；原摘要保持2befcc2a90f9e19909e89f55cc1f54035c1726664ce810b7aec1cb5d38235980，实际账号开通数0|
|微信小程序|本轮未发布|按用户要求不做运行验收，后续由用户上传新版才可使用新入口|

生产自动同步及历史保留PASS：13:58:23.856 DLT、13:58:24.982 SSQ成功，各120期本地缓存，错误为空，历史回填游标5；重启未清空缓存。仅剩当前后端镜像，上传临时目录为空；数据库、业务文件、配置、密钥、日志和数据备份保留。

权限测试PASS：创建实体即使显式给1仍落库默认0；读到旧1后维护者关闭为0，再保存资料不恢复1；普通Mapper强制赋1不生效。该字段仅维护者显式SQL配置，查询仍正常读1/0，无角色系统。

本轮最终无未解决必需FAIL/BLOCKED。后端/H5已实际发布；微信运行按用户指令排除且未发布。后续纯文档证据提交无需再发布任何服务或客户端。

权限补强发布构建统计：109总/95执行通过/14条件跳过，0失败/0错误；发布前含真实DEV的完整验证为109总/101执行通过/8条件跳过。
