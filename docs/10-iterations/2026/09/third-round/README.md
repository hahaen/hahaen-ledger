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
