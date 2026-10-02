# 第三轮工程审计报告

## 2026-09-30 追加：生产通知密钥示例

生产 env 示例补齐 `NOTIFICATION_KEY_AES_KEY` 占位项；代码绑定及 Compose 指向 `/home/hahaen/haji-prod.env` 静态核对 PASS。未修改服务器上的真实环境文件，生产配置与运行 NOT_RUN。详见 [通知中心迭代](../10-iterations/2026/09/notification-center/README.md)。

## 2026-09-30 追加：通知中心顶部卡片

通知中心复用个人中心的薄荷渐变引导卡，标题为“通知中心”，白色配置表单保持公共 20px 间距。类型检查、H5/微信生产构建以及实际登录态 H5 画面核对 PASS；[截图证据](../../app/tests/evidence/notification-center-card-20260930.png)。微信工具/真机视觉 NOT_RUN。详见 [本轮追加档案](../10-iterations/2026/09/notification-center/README.md)。

## 2026-09-30：通知中心与 Key 回显

通知配置已移出个人中心，独立入口紧跟“我的”页个人中心；GET 按当前有效用户解密回显 Key 并设置禁止缓存。6 项通知定向/DEV 集成测试、前端类型检查、微信转发18项与双端构建 PASS；H5 实际入口顺序及空配置画面 PASS，真实页面保存/再次进入及微信交互 PARTIAL/NOT_RUN。详情见 [本轮档案](../10-iterations/2026/09/notification-center/README.md)。


## 2026-09-29：通用通知配置补充

V9 通知配置主表及幂等关系表已在当前 DEV 执行；隔离 HTTP/数据库验证与 Java 25 定向测试通过。个人中心 Bark、pushplus 选填框和独立保存操作完成，前端类型检查及 H5/微信构建通过。普通后端全量测试因旧 `LoggingProfileConfigTest` 的 Windows 日志路径断言 FAIL；真实 H5/微信页面点击和通知发送 NOT_RUN。证据与边界见 [notification-config](../10-iterations/2026/09/notification-config/README.md)。

## 2026-09-29：物品清单实际净成本

清单金额改读已有 `netCostCents`：在役等于购入价，退役扣除二手售价，可为负数；详情购买价格与顶部在役购入总价不变。物品前端回归 5/5、Java 25 定向测试、类型检查和双端构建 PASS。H5 只读示例实际显示退役相机 `-200 元`，原价 `3,000 元`、售价 `3,200 元`；真实登录态 H5 和微信页面 NOT_RUN。详见 [迭代档案](../10-iterations/2026/09/item-list-net-cost/README.md)。

## 2026-09-29：已退役物品重新服役

详情页新增确认操作，后端在用户归属和状态校验后清空退役日期与二手价格，按在役口径重算成本。V8 保存重新服役请求键并已在 DEV 执行。PASS：14 项 Service 定向测试、隔离合成用户真实 HTTP/数据库闭环、V8 关系表结构审计、前端全量 87 项测试、类型检查与 H5/微信构建。FAIL：后端全量 68 项中 `LoggingProfileConfigTest` 在 macOS 下断言 Windows 固定路径。NOT_RUN：Chrome 点击控制超时，未完成真实 H5 交互；微信工具/真机未运行。详见 [迭代档案](../10-iterations/2026/09/item-reactivation/README.md)。

## 2026-09-29：物品价格输入格式

物品新增和编辑的购买价格占位为 `0`；编辑回显复用 `inputYuan`，整元不显示 `.00`，有角分保留两位。定向回归 9/9、类型检查和 H5/微信构建为 PASS。真实 H5 登录态与微信工具/真机视觉为 NOT_RUN。详见 [迭代档案](../10-iterations/2026/09/item-price-input-format/README.md)。

## 2026-09-20 追加：正式 MinIO 预签名 PUT 代理修复

- 已定位正式环境头像失败链路：`upload-url` 返回 200 后，公网 `/minio-api/` 的预签名 PUT 返回 403；后端 Endpoint、Bucket、时间、MinIO 可用性及 SigV4 内网 Host 均核对通过。
- 正式服务器 `/home/hahaen/nginx/conf.d/default.conf` 已先备份，再补充 `proxy_set_header Connection ""`、`chunked_transfer_encoding off` 和 `client_max_body_size 0`；`docker exec nginx nginx -t` PASS，并已 reload。
- PASS（运行态）：通过正式域名对随机临时对象执行同类 SigV4 PUT 返回 HTTP 200，MinIO `mc stat` 读回成功，临时对象清理成功；Nginx access log 同时记录该 PUT 为 200。
- PASS（运行态）：用户在正式 H5 页面重试后，日志取得 `upload-url 200`、真实预签名 PUT 200、`complete 200`、`view-url 200` 和对象 GET 200 的连续证据；本次未点击资料保存按钮，未将资料保存结果纳入结论。
- 详细档案：`docs/10-iterations/2026/09/minio-presigned-put-proxy/`。

## 2026-09-19 追加：生产后端日志挂载到宿主机

- `docker-compose.prod.yml` 将宿主机 `/home/hahaen/log/haji` 绑定到容器内同一路径，Jenkins 后续按现有 Compose 流程部署时自动生效。
- PASS（配置/静态）：Compose 挂载定义已补充；无业务、API、数据库或日志格式变化。
- NOT_RUN：未触发 Jenkins、未在服务器解析 Compose 配置，宿主机写入与容器重建后保留尚未验证。
- 注意：首次启用 bind mount 前应备份现有容器日志，挂载会隐藏原容器目录内容。
- 详细档案：`docs/10-iterations/2026/09/prod-log-host-bind/`。

## 2026-09-19 追加：MinIO 正式环境文件回显 URL

- 生产 MinIO SDK 仍访问 `MINIO_ENDPOINT` 内网地址；上传和预览的短时签名 URL 返回给客户端前替换为部署环境提供的 `MINIO_PUBLIC_URL_PREFIX`。
- Nginx 示例通过去除 `/minio-api` 前缀后代理到配置的同一 MinIO Endpoint，保留 `$proxy_host` 以匹配签名 Host。
- PASS（代码/静态）：URL 改写逻辑、生产配置和代理示例已同步；无数据库或前端接口结构变化。
- PASS（仅构建）：`cd server; mvn -q -DskipTests package` exit 0；自动化测试为 NOT_RUN。
- NOT_RUN：正式 Nginx 检查、MinIO 签名 PUT/GET 与真实页面回显尚未执行。
- 详细档案：`docs/10-iterations/2026/09/minio-public-preview-url/`。

## 2026-09-19 追加：新增记账页 H5 触摸滚动

- 新增记账页仅 `.entry-content` 是可滚动内容区，H5 页面使用固定数字键盘覆盖底部区域；H5 条件编译样式为该容器增加 iOS Safari 惯性触摸滚动。
- 微信小程序布局、导航、数字键盘、业务和接口均未修改。
- PASS（静态）：样式改动限定在 `#ifdef H5`，规范及本迭代档案已同步。
- NOT_RUN：自动化测试、类型检查、H5/微信构建和 iOS Safari 真机滑动验收均未执行。
- 详细档案：`docs/10-iterations/2026/09/h5-entry-touch-scroll/`。

## 2026-09-19 追加：账户负余额与信贷支出

- 资金账户支出、转账和还款可将余额扣至负数；支出允许选择信贷账户，超出可用额度会提示但仍允许保存。信贷欠款可超过总额度或为负数（溢缴余额）。
- 净资产统计将负资金余额与正信贷欠款计入负债，将负信贷欠款计入资产；退款、编辑及删除保留同一事务内的账户影响回退。
- PASS：后端全量 53 项测试、前端回归 56 项、TypeScript 检查、H5/微信构建；V5 静态约束及规范文档已同步。
- NOT_RUN：V5 Flyway/MySQL 实际执行与 `information_schema` 对照、DEV API/H5 登录态和微信开发者工具/真机验收。
- 详细档案：`docs/10-iterations/2026/09/negative-account-balances/`。

## 2026-09-19 追加：MinIO 新上传对象 Key 命名

- 新头像上传 Key 使用 `avatars/<上海时区yyyyMMdd>/<userId>/<原文件名主干>-<UUID>.<内容MIME扩展名>`；原文件名由既有上传请求携带，接口结构未变化。
- `app_file.user_id` 仍是归属依据；文件详情、完成、预览和删除仍通过当前用户过滤。历史对象 Key 和资料引用保持不变，无数据库迁移。
- PASS（静态）：后端生成逻辑及 API/数据库说明已同步。
- NOT_RUN：自动化测试、构建和真实 MinIO 上传/确认/预览未执行。
- 详细档案：`docs/10-iterations/2026/09/minio-date-filename-object-key/`。

## 2026-09-19 追加：微信小程序全页面自定义导航对齐

- 新增共享导航组件，通过 `uni.getMenuButtonBoundingClientRect()` 获取胶囊实际矩形；覆盖主 Tab 品牌页头、记账/详情/账户标题栏、关于与帮助、协议及个人中心，首次使用页也复用胶囊定位。
- 主 Tab 品牌图标与标语水平排列；顶部安全区按状态栏/安全区较大值计算，H5 保留原布局。
- PASS：前端回归 55/55、TypeScript 检查、H5/微信小程序生产构建、微信产物中的组件/API/CSS 静态核对。
- NOT_RUN：微信开发者工具/真机实际画面；不能用构建结论替代导航对齐运行态验收。详见 `docs/10-iterations/2026/09/wechat-custom-navigation-alignment/`。

## 2026-09-17 追加：微信小程序静态图片外置

- 已确认上传失败的构建目录为 3841.50 KB，超过报错接口的 2 MB 限制。主要来源是 `src/static/` 的 8 个 PNG，其中品牌图还出现一次构建重复副本。
- 已将固定视觉资源统一切换至 `https://hahaen.xyz/minio-api/haji/wx/` 下的对象；在迁移前逐一执行 HEAD，8 个对象均为 HTTP 200 且 Content-Length 与本地原图一致。原图保留于 `app/offloaded-static-assets/wx/`，不再参与 uni-app 小程序构建。
- PASS：前端回归 55/55、`pnpm run typecheck`、`pnpm run build:mp-weixin`、构建目录体积核对 270.19 KB、`git diff --check`。未改动数据库、后端、API、认证、用户头像上传或账务逻辑。
- NOT_RUN：未重新导入微信开发者工具执行上传，也未在真机验证远端图片加载。详细档案：`docs/10-iterations/2026/09/mini-program-static-image-offload/`。

## 2026-09-17 追加：新增记账页微信小程序顶部控件

- 新增记账页将 `screen-nav` 移到 `.entry-content` 外，避免顶部导航与主体滚动层混用；微信小程序条件编译为页面状态栏后的自定义导航预留 44px，类型切换按钮显式锁定 38px 高度、零内边距、单倍行高、flex 居中和 `::after` 清理。
- 未修改新增记账的类型切换业务、保存逻辑、接口、金额、事务、数据库或认证逻辑；H5 仅共享结构保持可构建。
- PASS：前端回归 53/53、类型检查、H5 生产构建、微信小程序生产构建、`entry.wxml/app.wxss` 产物静态核对和 `git diff --check`。
- BLOCKED / NOT_RUN：未在真实微信开发者工具/真机打开新增记账页并点击支出、收入、转账；当前 UI 自动化面未暴露可控制的微信开发者工具原生窗口。

## 2026-09-17 追加：微信小程序个人中心底部按钮可见性

- 个人中心保存按钮和修改密码确认按钮保留原有节点、文案及事件，仅将主按钮背景改为明确的 `#49ad9c`，并清理微信原生 `button::after`，修复截图中白色空按钮现象；H5 视觉规则保持不变。
- 个人中心头像已改为通过 `uni.chooseImage` 选择相册/相机图片，工具层完成临时文件校验和预签名 PUT，继续保持预览后保存关联；没有调用微信头像资料接口。
- 针对小程序控制台的废弃 API 提示，已将文件信息读取改为 `getFileSystemManager().getFileInfo`；预签名 PUT 设置 `dataType: 'text'`，并补充上传失败状态提示。
- PASS：前端回归 52/52、类型检查、H5 生产构建、微信小程序生产构建，以及 `profile.wxml`/`profile.js`/`utils/file.js` 产物静态核对。
- BLOCKED / NOT_RUN：当前 UI 自动化面未暴露可控制的微信开发者工具原生窗口，未完成真实小程序页面、登录态和按钮点击验收。详细记录见 `docs/10-iterations/2026/09/global-ui-cross-platform-optimization/`。

## 2026-09-15 追加：微信小程序默认首页入口

- `app/pages.json` 已将 `pages/index/index` 调整为首路由；`pages/first-use/first-use` 仍保留注册，但不再作为默认入口。
- `app/src/App.vue` 已移除小程序启动登录成功和登录重试成功后的首次使用页跳转，微信静默登录、Token 恢复和失效重登逻辑保持不变。
- PASS：前端流程回归 33/33、`pnpm run typecheck`、`pnpm run build:mp-weixin`；真实微信开发者工具重新打开后的人工画面确认仍为 NOT_RUN。完整档案：`docs/10-iterations/2026/09/default-mini-home/`。

## 2026-09-15 追加：微信小程序默认登录链路修复

- 小程序启动时通过 `uni.login` 获取一次性 code，前端请求 `/api/app/auth/wechat-mini/login`；后端通过微信 code2Session 换取身份，按 `user_identity` 创建或复用本地用户，再签发 Sa-Token。H5 账号、密码、验证码认证路径未修改。
- `AppSecret` 只从服务端配置读取；`session_key`、`open_id` 不返回前端；登录失败记录 `WECHAT_MINI_PROGRAM` 审计日志且不记录 code 或微信原始错误信息。
- 已定位并修复真实运行报错：微信 `jscode2session` 返回 `text/plain` 时，旧实现直接按 JSON 类型读取而触发 `UnknownContentTypeException`；现改为接收文本后由 Jackson 解析，并对配置缺失、响应解析异常写入不含 Secret/code 的安全日志。
- 已补齐 Token 失效链路：小程序会重新获取微信 code、重新签发业务 Token，并只重试原业务请求一次；登录过程不再嵌套刷新请求，避免自动重登录死锁。
- PASS：本机后端无效 code 探针返回正确 `WECHAT_CODE_INVALID`；后端 `mvn test` 47/47，前端回归 42/42，TypeScript 检查，H5 和微信小程序生产构建；本机 MySQL/Redis/MinIO/Flyway 运行探针通过。
- PASS：微信开发者工具真实运行已取得有效 `wx.login` code；首次登录创建 1 条身份关联，重复登录成功记录归属同一系统用户；真实 Token 访问账户/资料接口 HTTP 200；删除临时 Token 映射后重新运行，成功登录数增加且新 Token 业务访问 HTTP 200。
- 详细档案：`docs/10-iterations/2026/09/wechat-mini-auth/`。

## 2026-09-13 追加：H5 已登录默认入口回首页

- 已识别到启动层只在根路径 `/` 存在有效 token 时进入首页，浏览器重新打开且仍落在登录页时会错误保留登录页；本轮将登录和注册入口纳入同一启动重定向规则。
- 业务页和协议页不在该重定向范围内，继续按已有 H5 会话持久化规则保留当前 hash 路由。
- PASS：`auth-guard.test.mjs` 5/5、`pnpm run typecheck` 和临时注入本地 API 地址后的 H5 生产构建均完成。NOT_RUN：未取得有效会话，未执行真实浏览器关闭页面后重新打开的端到端验收；最终状态以 `docs/10-iterations/2026/09/h5-authenticated-entry-home/09-verification.md` 为准。

审计日期：2026-09-07。本次补充审计聚焦 H5 我的页、关于与帮助、用户累计天数、头像文件和退出登录，并追加首页、日历、资产三模块闭环；不把未执行的运行联调写成 PASS。

## 2026-09-13 追加：HTTP 临时认证兼容

- H5 注册和登录在无 Web Crypto 的 HTTP 浏览器下可使用独立 `compatibilityPassword` 混淆载荷，前端请求 JSON 不再传递明文字段；浏览器具备 Web Crypto 或页面为 HTTPS 时仍使用既有 RSA-OAEP。
- 服务端开关 `H5_ALLOW_INSECURE_PASSWORD_OVER_HTTP` 默认关闭，仅在明确开启且传输被识别为 HTTP 时解开兼容载荷，然后立即复用既有密码长度校验、BCrypt 哈希、验证码和审计流程。HTTPS 请求、关闭开关和双密码字段均拒绝。
- PASS：认证定向单测 4/4、后端全量 43/43、前端流程 30/30、类型检查和 H5 生产构建。NOT_RUN：服务器实际重启、HTTP 注册/登录以及后续 HTTPS/RSA 端到端验证。
- 该兼容载荷不是密码学加密，无法抵抗 HTTP 中间人；只可作为用户明确要求的临时过渡，部署 HTTPS 后必须关闭环境开关。完整档案：`docs/10-iterations/2026/09/http-temporary-auth-compatibility/`。

## 2026-09-12 追加：Jenkins 流水线兼容性

- Jenkins 已从 Gitee 实际获取 `server/Jenkinsfile`，证明分支参数和脚本路径可用；此前选择 `origin/main` 造成 Git 将其解释为 `refs/heads/origin/main`，已改为分支值 `main`。
- 控制器未安装 Timestamper 插件，实际解析拒绝 `timestamps()`；前后端 Jenkinsfile 已删除该非必要选项，避免为了日志时间格式新增插件依赖。
- 当前状态为 PARTIAL：修正尚待提交到 Gitee 并重新触发；SSH、Docker Compose 和健康检查尚未运行，不能视为部署成功。

## 2026-09-13 追加：Nginx 入口配置说明对齐

- `deploy/nginx/haji-api-location.conf.example` 已同步为当前完整虚拟主机示例：`map` 在 `http {}` 上下文，Jenkins、`/haji-api/` 和 H5 SPA 路由位于同一个 `server {}`。
- `/jenkins/` 代理保留路径前缀，匹配 Jenkins 的 `--prefix=/jenkins`；`/haji-api/` 使用带末尾 `/` 的 `proxy_pass`，因此浏览器的 `/haji-api/api/app/...` 会转成后端既有 `/api/app/...`。
- PASS（静态）：仓库示例和部署档案已对齐。NOT_RUN：未执行服务器上的 `nginx -t`、reload 以及 Jenkins/H5/API 的真实 HTTP 验收。

## 本轮结论

- 我的页与原型的个人卡片、累计天数、更多分组、关于与帮助、退出登录已落到 H5 页面。
- 累计记账天数由服务端 `ProfileService` 依据当前用户最早有效账单的 `occurred_at` 业务日期计算，首个记账日计为第 1 天；尚无有效账单时为 0。
- 头像上传复用现有 MinIO 预签名 URL 链路，文件查询补充当前用户和逻辑删除过滤；前端仅使用短时效预览 URL。
- H5 退出登录在 store 中清理服务端会话对应的本地令牌和业务状态，mine 页保持当前页面并显示“登录”。
- H5 刷新有效会话时不再无条件跳首页；Sa-Token 会话改用 Redis DAO，并对 Redis Key 增加 `haji:` 前缀。
- 用户启动日志发现 Redis DAO 自动配置与自定义 DAO 重复注册；已通过排除自动配置保留单一自定义 DAO，避免 Spring 上下文启动失败。
- 前端 typecheck、H5 构建、后端编译和测试 PASS；真实 H5 登录态刷新为 PARTIAL，MySQL 账务联调和微信开发者工具验收仍为 BLOCKED/NOT_RUN；Redis PING 与 MinIO bucket 探针已在独立端口启动时成功，但真实对象/账务链路未执行。

## 2026-09-11 追加：累计记账天数统计口径修正

- 用户明确要求累计记账天数基于最早一次记账，而不是账号创建时间；服务端现在查询当前用户最早未逻辑删除账单的 `occurred_at`。
- 统计按该业务日期到当天的自然日含首日计算；没有有效账单或仅有未来日期账单时返回 0。
- 不修改 `ProfileVO`、前端页面、数据库表或 Flyway；`GET /api/app/user/profile` 的 `cumulativeDays` 字段仅更新统计语义。
- PASS：`ProfileServiceTest` 3 项和 `mvn test` 29 项均通过。NOT_RUN：真实登录态请求及 MySQL 历史账单复核。

## 本轮追加：首页、日历、资产模块

- 后端新增账户、资产、账单、退款、首页汇总和日历查询域，严格映射既有 V3/V4 表；写操作包含当前用户归属、逻辑删除、金额分、账户类型、幂等和事务边界。
- 首页已支持月度切换、服务端汇总、有效退款金额、按日倒序分组、空/错/加载态；日历已改为周日开始的 42 格月历，支持今日/选中/非当月日期、月度标记和单日接口；资产页由后端返回净资产、总资产、总负债，并支持资金/信贷分组、账户表单、流水筛选和部分还款。
- 新增 `AccountServiceTest`、`AssetServiceTest`、`TransactionServiceTest`、`HomeServiceTest`、`CalendarServiceTest` 和 `GlobalExceptionHandlerTest`；最终 Maven 18 tests 全部通过。
- 独立端口 18080 在 `spring.flyway.enabled=false` 下启动成功，Redis PING、MinIO bucket 探针成功，未登录账户接口返回 401，OpenAPI 暴露本轮 12 个路径。
- 本轮未执行 Flyway/DDL，Migration diff 为空；没有隔离测试用户，因此真实账单写入、跨用户/并发事务仍保持 BLOCKED/NOT_RUN；微信开发者工具登录、重复登录、业务 Token 和失效重登已单独取得运行证据。

## 本轮差异与待确认

1. V3 没有独立账户停用状态，当前账户“删除/停用”统一落为 `deleted=1`；历史账单仍保留，但删除账户详情不再作为有效账户访问。
2. V3 没有余额补齐流水的业务标识，编辑余额当前只更新余额列，没有伪造一条会污染统计口径的普通账单；若必须保留校准流水，需要后续数据库设计变更。
3. V3 没有账户名称唯一索引；本轮在 Service 层按当前用户和有效账户做名称重复校验，但无法替代数据库级并发唯一约束。
4. AI 设计稿和原型的逐区域截图、微信安全区和平台交互尚未完成，状态不是 PASS。

## 仍需人工验收

1. 启动 MySQL、Redis、MinIO，使用真实 H5 会话验证刷新保留当前路由、后端重启续会话、个人资料、头像上传/刷新、旧预览 URL 过期重取和退出后的“登录”状态。
2. 在 320/375/414px 下截图对照原型，检查滚动、安全区和帮助页长内容。
3. 在微信开发者工具中完成头像主动选择、上传、预览和保存后的真实运行验收，不把 H5 文件选择器直接移植到微信端。

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
- 2026-09-08 历史审计曾发现小程序 Store 调用 `/api/app/auth/login` 而后端没有对应路径；该缺口已修复为 `/api/app/auth/wechat-mini/login`，并在 2026-09-15 定位修复 `text/plain` 响应解析错误，真实微信联调随后已在本机开发者工具完成。
- 发现 `asset_account` 数据库没有账户名称唯一索引，但当前 `AccountService` 对有效账户执行重名校验；当前规范已明确这是 Service 约束并列出并发竞态风险。
- 发现 `docs/09-audit/README.md` 原引用不存在的 `second-round-audit.md`，已改为说明历史明细未保留；当前工作区也未找到报告曾引用的 `app/tests/evidence/*.png`，因此视觉截图不再作为本次可复核 PASS。
- 当前无可复核的 MySQL 3306、Redis 6379、MinIO 9000 运行证据，Flyway history、`information_schema`、真实对象链路、有效 H5 会话刷新和微信开发者工具保持 `BLOCKED`/`NOT_RUN`。

## 认证密码控件补充（2026-09-08）

- 登录页和注册页共用的 `AuthPage.vue` 已将密码右侧控件改为透明背景的 CSS 小眼睛图标；隐藏状态为斜线眼睛，点击后切换为明文显示，按钮保留 `aria-label` 和 `title`。
- 本次不改认证接口、密码加密、数据库或权限逻辑；共享组件静态核对、前端类型检查、H5/微信小程序生产构建，以及本机 H5 登录/注册页面点击切换均已完成。真实认证提交和微信开发者工具人工验收仍未执行。

## 认证页说明文字移除（2026-09-08）

- 已从登录/注册共用页面移除“微信小程序会自动完成微信登录，无需账号密码”和“注册仅需要账号、密码和验证码，不收集昵称或头像”两条底部说明文字，保留账号、密码、图形验证码和登录/注册操作。
- 同步删除无引用的 `.auth-footnote` 样式；不改变 H5 认证接口、注册字段、密码加密、数据库或微信登录接口状态。

## H5 浏览器主应用图标（2026-09-08）

- `app/index.html` 已增加标准 PNG favicon 声明，统一复用 `app/static/brand.png` 主应用图标。
- 本机 H5 入口的 favicon DOM 声明和 `/static/brand.png` HTTP 200 资源已核对；主图标已替换为真透明 PNG，源图标和 H5 生产构建 favicon 四角 alpha 均为 0。
- 前端类型检查和 H5 生产构建 PASS；真实生产域名/CDN 缓存刷新未执行，记录为 NOT_RUN。
- 本次不涉及数据库、API、后端、认证、权限、微信小程序和业务页面。

## 底部导航设计稿对齐（2026-09-08）

- 根因是 H5/uni-app 渲染的 `button` 平台默认外观使四个导航项显示为独立圆角卡片，与设计稿的连续底栏不一致。
- `app/src/prototype.scss` 已将 `.nav-item` 设为四项等宽 flex 子项，并显式清除导航整体及按钮的边框、圆角、阴影、默认 appearance、内边距和背景；当前项图标选中态保持不变。
- 本机只读视觉服务页面已核对：4 个导航项计算宽度均为 103px，导航整体和按钮均无外部边框，页面底部为连续白色导航栏。
- 前端类型检查、H5 生产构建和微信小程序生产构建均 PASS；逐区域截图未持久化到 `app/tests/evidence/`，因此设计截图证据保持 PARTIAL，不宣称视觉截图 PASS。

## 资产新增弹窗设计对齐（2026-09-08）

- 资产页右下角新增入口已从“选择账户类型后跳转新增页”调整为原型风格的“新增资产账户”表单弹窗。
- 弹窗支持资金账户/信贷账户切换、账户名称、余额或额度、计入净资产、取消和保存；保存复用现有账户创建接口，并在成功后刷新资产概览。
- 弹窗已按参考稿细化为动态标题、铺满的紧凑 Tab、空值占位、必填校验及居中操作按钮；金额输入不附加钱币图标。
- 账户详情页保留用于查看、编辑和还款，已删除其中无用的新增模式；记账页无账户时不再跳转旧新增页面，而是回到资产页使用新增弹窗。
- 本次未修改数据库、后端、API、金额计算或权限逻辑；前端类型检查 PASS。
- H5 生产构建在临时设置本地 `VITE_API_BASE_URL` 后 PASS；有效登录态下的浏览器视觉复核和微信开发者工具验收为 NOT_RUN，详见 `docs/10-iterations/2026/09/asset-create-modal-design/`。

## 资产账户顺序（2026-09-08）

- 新增 V5 `asset_account.sort_order`，旧有效账户按稳定顺序回填，新建账户在同类账户末尾追加。
- 账户列表和资产概览按账户类型、顺序、ID返回；新增 `PUT /api/app/accounts/{id}/order`，在事务内锁定并交换同类账户，带期望顺序保护重复请求。
- 资产页支持长按账户进入换序状态，再点击同类账户完成交换；普通点击仍进入账户详情。
- 本次代码静态核对完成；后端测试、前端类型检查/构建、Flyway 实际执行和有效会话长按人工验收需以本轮命令和运行环境证据为准，不提前宣称 PASS。

## 账户详情布局（2026-09-08）

- 账户详情页顶部导航已局部压缩，不影响其他页面；“停用账户”改为“删除账号”，底层仍为逻辑删除。
- 编辑账户已切换为新增账户弹窗同款布局，并隐藏账户类型 Tab；未修改数据库、API 或删除策略。
- 前端类型检查和 H5 生产构建 PASS；有效登录态下的视觉点击验收保持 NOT_RUN。

## 删除账户确认弹窗（2026-09-08）

- 已将系统原生删除确认改为参考稿风格的自定义圆角弹窗，包含删除说明、取消按钮和红色“确认删除”按钮。
- 删除确认仍调用既有逻辑删除接口，不修改数据库和历史流水保留规则；有效会话视觉验收保持 NOT_RUN。

## 资产账户换序交互微调（2026-09-08）

- 移除长按后页面顶部的“已选择账户”提示条。
- 长按信贷账户时，仅信贷账户列表显示“交换”；长按资金账户时，仅资金账户列表显示“交换”，另一组保持进入详情的箭头。
- “交换”文字缩小至 11px 并使用更深的文字色，降低透明感；本次微调后的前端类型检查、H5 构建和微信小程序构建均通过。
- 换序状态下只有右侧“交换”文字响应换序，点击账户其他区域不再触发操作；普通状态仍支持整行进入账户详情。

## 账户 ID 精度修复（2026-09-08）

- 根因为 Java 雪花 ID 以 JSON 数字返回后被浏览器 JavaScript Number 舍入，页面跳转时携带了错误账户 ID；并非数据库表缺失。
- 账户、流水、退款、登录用户、个人资料和文件响应中的实体 ID 已改为 JSON 字符串；数据库 Entity、内部 Long、归属校验和金额逻辑保持不变。
- `pnpm typecheck` PASS，`mvn test` 19/19 PASS；数据库无变更，真实登录态点击验收保持 NOT_RUN。
- 详细档案见 `docs/10-iterations/2026/09/account-id-precision-fix/`。

## 还款弹窗与账户选择（2026-09-08）

- 还款系统弹窗已改为参考稿风格，增加欠款说明、还款账户展示、金额输入和确认还款按钮。
- 新增选择还款账户弹窗，资金账户按有效状态筛选并展示余额；确认后仍调用既有还款接口，未改变后端业务逻辑。
- 前端类型检查和 H5 构建 PASS；有效登录态视觉验收保持 NOT_RUN。

## 前端金额展示统一（2026-09-08）

- 复用 app/src/utils/money.ts，由 formatYuan() 统一生成不带符号的金额文本；不再由页面分别判断 .00、调用 toFixed 或拼接货币符号。
- 已覆盖首页日均/收支、日历日汇总、资产净资产/总资产/总负债、资金账户/信贷账户余额和额度、交易列表、账单详情、退款、还款和记账编辑/计算展示。
- 10000 分展示为 100，9950 分展示为 99.50，9999 分展示为 99.99，0/null/undefined 安全展示为 0；字符串与数字输入结果一致。
- 本次未修改后端接口、数据库字段、整数分传输和实际计算精度。前端类型检查、H5 生产构建、微信小程序生产构建和格式化边界样例均 PASS。
- 静态原型目录 哈记账小程序_原型设计稿/ 是独立设计参考，不属于 app 构建入口，本次未改动；真实登录态视觉验收和微信开发者工具人工验收为 NOT_RUN。

## 金额图标移除（2026-09-08）

- 公共金额展示组件 app/src/components/MoneyDisplay.vue 统一只渲染金额数值和必要的正负/文字前缀，不再渲染钱币图标。
- 已覆盖首页、日历、资产、账户详情、账单详情、退款、还款、记账页和公共 TransactionRow；资产、账户、还款和记账输入框也不再附加 `¥`。
- 未修改金额计算、接口、数据结构和提交值；移除无效的货币图标样式与带符号格式化入口，并完成运行时前端静态检索。
- 前端类型检查、H5 构建和微信小程序构建通过；真实登录态截图和微信开发者工具验收保持 NOT_RUN。
- 本次未修改后端、API、数据库、金额计算或数据结构。静态原型目录按项目规范属于独立设计参考，未纳入 app 构建修改范围。

## 核心页面原型与记账闭环修复（2026-09-08）

完成首页/日历状态和布局、千元编辑/退款、还款编辑、重复保存防护、写入后刷新语义、日历选择同步、退款删除锁内复查及帮助未开放提示。资产和我的页面无差异。PASS：前端 9 项回归、后端 24 项单测、类型检查、双端构建、只读视觉截图。PARTIAL：真实端到端账务；BLOCKED：缺少可用测试登录会话；NOT_RUN：数据库并发事务及微信工具。完整证据与回滚见 [core-pages-prototype](../10-iterations/2026/09/core-pages-prototype/README.md)。

## V5 启动校验修复（2026-09-08）

PASS：移除 V5 首行误加的 cd，恢复数据库既有 checksum 266153221；mvn test package 25 项测试通过。实际 DEV 启动验证 5 个迁移全部通过、Schema 保持 V5 且无需迁移、应用成功启动；临时 18080 未登录账户接口返回 401，验证后停止实例。未执行 repair 或修改数据库历史。记录：docs/10-iterations/2026/09/flyway-v5-checksum/README.md。

## 首页交互修正（2026-09-08）

首页“当前年/月 · 日均消费”为普通文字，不提供月份选择；最近记账右侧不显示刷新按钮。年月与查询范围在加载时按当前日期更新。类型检查、H5 构建 PASS。档案：docs/10-iterations/2026/09/home-static-month/README.md。

## 首页摘要布局与当前月份核对（2026-09-08）

PASS：修复首页金额组件默认 `inline-flex` 导致日均消费金额与标题同一行的问题，金额现独占一行并位于标题下方；本月支出/收入改为按内容宽度排列，避免间距过大。静态代码核对确认首页初始化和加载时均从 `localDateTime()` 取得 `YYYY-MM`，并将其传给 `/api/app/home/summary`；`2026 年 9 月` 是当前业务日期的动态结果，不是硬编码。类型检查、H5 构建和独立只读视觉页面核对通过。真实认证账务和微信开发者工具验收仍为 PARTIAL/NOT_RUN。档案：docs/10-iterations/2026/09/home-summary-layout-current-month/README.md。

## 首页最近记账设计稿对齐（2026-09-09）

PASS：恢复首页最近记账日期行的设计稿边距、字号和单行收支汇总，并显式统一滚动容器、内容层和账单行的 `#f9fcfb` 浅薄荷底色，避免平台默认白底造成视觉差异。类型检查、H5 构建和独立只读视觉页面核对通过；真实认证账务和微信开发者工具验收仍为 PARTIAL/NOT_RUN。档案：docs/10-iterations/2026/09/home-recent-list-design-alignment/README.md。

## 账单详情顶部对齐（2026-09-09）

账单详情页已复用新增记账页的紧凑顶部导航规格：最小高度 44px、标题字号 16px、返回按钮最小高度 36px，且不改变账单内容区、接口或后端逻辑。`pnpm run typecheck`、临时注入本地 API 地址后的 H5 构建和只读视觉夹具核对均为 PASS；原开发地址没有有效 H5 会话，真实账单视觉验收为 BLOCKED。档案：docs/10-iterations/2026/09/transaction-detail-header-alignment/README.md。

## 账单详情删除弹窗样式统一

账单详情页的账单删除和退款删除确认已改为复用账户详情页的自定义圆角弹层，确认后继续调用既有删除接口，删除失败时保留弹层供重试。前端类型检查、H5/微信小程序构建 PASS；有效登录态视觉验收保持 NOT_RUN。详细记录见 `../10-iterations/2026/09/transaction-detail-delete-modal/`。

## 账单详情退款弹窗设计对齐（2026-09-09）

- 退款弹窗已由通用居中弹窗调整为专用圆角弹层，按系统现有薄荷绿视觉令牌提供把手、标题、退款说明、大金额输入框和双按钮布局。
- 退款记录区增加 `REFUND RECORD` 英文眉标题，改用暖色退款卡片、累计退款汇总行和退款明细层级。
- 打开弹窗时默认回填当前剩余可退款金额；金额转换、退款上限校验、幂等键、错误保留和既有退款接口未改变。
- 前端回归测试 15/15、TypeScript 类型检查、H5 生产构建和微信小程序生产构建已实际通过。
- 视觉夹具已启动，但 Codex 内置浏览器访问本机地址返回 `ERR_BLOCKED_BY_CLIENT`，因此截图视觉验收为 BLOCKED；真实登录态退款写入验收为 NOT_RUN。

## 退款记录删除失败修复（2026-09-10）

- 根因一是详情页删除成功后未释放 `saving`；根因二是详情和账户刷新使用 `Promise.all`，账户刷新失败会阻断成功删除后的详情更新。
- 前端现在在 DELETE 成功后立即从当前详情移除目标退款并回算累计退款、有效金额和退款状态；详情刷新与账户刷新解耦，刷新失败不会回显已删除记录。
- 删除接口失败时，前端确认弹窗直接显示后端业务错误；后端软删除影响行数不为 1 时按失败处理，避免误报成功。
- 后端继续通过 `deleteRefund()` 事务完成退款软删除、账单有效金额回算和资金账户余额恢复，没有改动 API 路径或数据库结构。
- 证据：前端 Node tests 18/18、`pnpm run typecheck`、H5/微信生产构建 PASS；Maven tests 26/26 PASS。
- 真实登录态退款删除、MySQL 实际事务与 `information_schema` 对照仍为 NOT_RUN/BLOCKED，不能替代端到端验收。

## 编辑记账类型固定（2026-09-09）

- 账单详情点击“编辑”后，编辑页继续通过账单详情接口回填 `EXPENSE`、`INCOME`、`TRANSFER` 或 `REPAYMENT` 类型。
- 编辑态隐藏顶部支出/收入/转账切换器，并在 `setType()` 增加编辑态保护；新增态类型切换保持不变。
- 编辑页标题下方显示只读的“当前账单类型：{类型}记账”提示，且只在回填完成后显示。
- 前端回归 15/15、TypeScript 检查、H5 生产构建和微信小程序生产构建均为 PASS。
- 本轮无数据库、API、后端、权限、金额和事务变更；真实登录账单操作与微信开发者工具人工验收为 BLOCKED。
- 详细档案：`docs/10-iterations/2026/09/transaction-edit-type-lock/`。

## 日历页固定上半部分与账单独立滚动（2026-09-10）

- 日历页改为视口高度的纵向 flex 容器并隐藏页面溢出；页头、月历、当日汇总和日期标题均不可收缩。
- 账单记录由普通 `view` 改为独立纵向 `scroll-view`，只允许记录区域滚动；无数据库、API、后端或金额逻辑变更。
- 布局回归与既有前端回归 19/19、`pnpm run typecheck`、H5 生产构建 PASS；独立只读夹具观测到页面滚动保持 0、记录滚动节点发生滚动。
- 未生成可提交 PNG 截图，视觉截图证据为 NOT_RUN；微信开发者工具人工验收为 NOT_RUN。
- 详细档案：`docs/10-iterations/2026/09/calendar-records-scroll/`。

## 账户详情固定筛选与流水独立滚动（2026-09-10）

- 资金账户和信贷账户共用的账户详情页将账户卡片、账户流水标题及五项筛选按钮固定在记录滚动容器外。
- 日期/笔数行与交易记录一起放入独立纵向 `scroll-view`；无数据库、API、后端、金额和筛选查询逻辑变更。
- 布局回归与既有前端回归 20/20、`pnpm run typecheck`、H5 生产构建 PASS；独立只读夹具观测页面滚动保持 0、流水滚动节点发生滚动，并确认日期分组标题吸顶。
- 未生成可提交 PNG 截图，视觉截图证据为 NOT_RUN；微信开发者工具人工验收为 NOT_RUN。
- 详细档案：`docs/10-iterations/2026/09/account-records-scroll/`。

日期分组标题补充吸顶：`.account-record-list .date-heading` 使用 `position:sticky; top:0; z-index:2`，当前分组在本组记录滚动期间保持可见，下一分组进入时替换旧标题。

## 首页重复加载请求修复（2026-09-10）

- 根因：H5 启动阶段和首页 `onShow` 都调用 `ledger.refresh()`，每次调用都会并行请求账户和当月摘要，因此 Network 面板显示两条相同的 `accounts` 与 `summary` 请求。
- 已移除 H5 `App.vue` 的启动预取；启动流程现在只恢复会话并处理根路径跳转，首页 `onShow` 是首次业务数据加载入口。
- 补充根因：日历页为展示当月交易类型圆点会调用同月刷新；快速切回首页时两次刷新并发，故仍会看到相同接口。Store 现在合并同月进行中的刷新，首页自动显示复用日历已加载的同月摘要；下拉刷新和重试保持强制请求。
- PASS：首页重复加载回归测试 17/17、静态调用链核对、`pnpm run typecheck`、临时注入本地 API 地址后的 `pnpm run build:h5`。构建仅输出既有 Sass legacy-js-api 弃用警告。
- NOT_RUN：有效登录态浏览器 Network 面板刷新验收；当前本机未取得测试会话。完整档案见 `docs/10-iterations/2026/09/home-duplicate-load-fix/`。

## 退款逻辑删除标识修复（2026-09-10）

- 真实开发库中，退款 `2097713985701609473` 的 `deleted_at` 已有值但 `deleted=0`，所以它仍满足详情退款查询的有效记录条件；库内共 3 条退款有相同不一致状态。确认是后端删除落库缺陷，不是前端缓存。
- 根因是退款路径以 `BaseMapper.updateById` 写入带 `@TableLogic` 的 `deleted` 字段，该字段未进入普通更新集合，只留下删除审计时间。
- 已新增显式条件软删除 SQL，同时覆盖单笔退款删除与账单删除的退款级联路径；SQL 原子地要求原记录 `deleted=0`，影响行数不为 1 会阻断余额回算。
- 删除后会按剩余有效退款回算 `hasRefund`；最后一笔退款删除后置为 0，支出交易行不再显示“退”。
- PASS：`mvn test` 26/26，且交易测试确认调用显式软删除而非 `updateById`。NOT_RUN：当前 8080 实例需要重启后才能验证真实 DELETE；为避免中断现有调试进程，本轮未重启或对用户数据重试写入。
- 完整档案：`docs/10-iterations/2026/09/refund-logical-delete/`。

## 首页最近记账双月分段加载（2026-09-11）

- 首页摘要继续只请求和展示当前月；最近记账改为独立的 `GET /api/app/home/recent-transactions` 查询，首次覆盖当前月和上月。
- 返回的 `startMonth` 作为排他游标；记录区触底后请求前一个连续双月段，按账单 ID 去重追加，并依据 `hasMore` 停止加载。加载、加载更多失败与摘要失败状态彼此独立。
- 后端查询由当前 Sa-Token 用户限定，并在账单时间段查询和更早记录判断中都排除 `deleted=1` 数据。
- PASS：`HomeServiceTest` 游标范围测试、`mvn test` 27/27、前端动态滑动回归 18/18、类型检查以及 H5/微信小程序构建。NOT_RUN：真实登录态下的网络请求、滑动触底与账单数据联调。
- 完整档案：`docs/10-iterations/2026/09/home-recent-transactions-infinite-scroll/`。

## 我的页退出登录弹窗设计对齐（2026-09-11）

- “我的”页退出登录已从平台原生 `uni.showModal` 改为项目统一圆角确认弹层，复用现有薄荷遮罩、顶部提示条、白色面板和取消/危险确认按钮。
- 退出入口只打开弹层；确认后才调用既有 `ledger.logout()`。退出中锁定遮罩关闭和重复确认，完成后仍留在当前页并呈现未登录状态。
- PASS：`node --test tests/page-flows.test.mjs` 19/19、`pnpm run typecheck`、H5 和微信小程序生产构建。
- BLOCKED：本机只读视觉夹具被内置浏览器以 `ERR_BLOCKED_BY_CLIENT` 拦截，未得到运行时截图。NOT_RUN：真实登录态注销操作。
- 完整档案：`docs/10-iterations/2026/09/mine-logout-modal-design/`。

## 我的页个人中心设置行对齐（2026-09-11）

- 个人中心改用和关于与帮助、退出登录相同的设置按钮组件，并移除专属样式，避免平台组件造成底色、边框和圆角差异。
- 关于与帮助仍保留 `openHelp` 跳转，退出登录仍保留既有弹层和注销流程；本次不改动其模板或样式。
- PASS：`node --test tests/page-flows.test.mjs` 20/20、`pnpm run typecheck` 均通过。完整档案：`docs/10-iterations/2026/09/mine-profile-center-list-alignment/`。

## 个人中心资料与密码管理（2026-09-11）

- 我的页个人中心入口已进入独立资料页；页头复用关于与帮助的返回、居中标题和右侧占位规格，主体包含头像、昵称和账号资料卡，以及固定的修改密码/保存操作栏。
- 保存资料时头像、昵称和账号均为前端必填；资料与密码接口均只使用当前 Sa-Token 用户。账号已有值时前后端均禁止修改；账号为空时，前端要求先输入账号并点击修改密码，后端将首次账号与密码在同一事务中写入，应用层预检与既有唯一索引共同保证不重复。
- 密码在自定义圆角弹层确认后立即通过 RSA-OAEP 密文接口更新；资料保存成功显示自定义成功弹层，确认后返回我的页。头像复用既有 H5 上传，真实 MinIO 和微信头像入口未扩展为已验收能力。
- PASS：`mvn test` 33/33、前端 Node 测试 25/25、类型检查、H5/微信生产构建，以及只读视觉夹具主页面和密码弹层核对。NOT_RUN：真实登录态资料/密码写入、真实头像上传和微信开发者工具。
- 完整档案：`docs/10-iterations/2026/09/profile-center-management/`。

### 个人中心首次密码输入补充（2026-09-11）

- 资料接口新增仅表明状态的 `passwordConfigured`，不返回密码内容或密码哈希。未设置密码时，账号下方显示首次密码框，保存资料会在同一事务中写入首次账号、密码和昵称，底部仅显示保存；已设置密码时，该输入框隐藏并恢复修改密码按钮。
- 首次密码框和修改密码弹层均复用登录页密码显隐小眼睛及无障碍标签。`mvn test` 34/34、`node --test tests/page-flows.test.mjs` 21/21 和 `pnpm run typecheck` 已通过；真实登录态写入、MinIO 上传和微信开发者工具仍为 NOT_RUN。

## H5 退出登录后的路由访问限制（2026-09-11）

- H5 现在把登录页和注册页作为唯一未登录白名单；无 token 访问首页、日历、资产、我的、记账、详情、账户或帮助时，统一跳转登录页。
- 全局守卫监听 H5 hash 路由、浏览器历史变化和页面恢复，覆盖刷新、直接地址、前进/后退及会话失效后的业务路由恢复。
- 我的页手动退出在本地会话清理后 H5 `reLaunch` 登录页；微信小程序继续保留原有重新登录流程。
- PASS：路由守卫与现有页面回归 28/28、类型检查、H5 和微信小程序生产构建、`git diff --check`。
- NOT_RUN：真实 H5 登录态下的注销、浏览器前进/后退和直接地址运行验收；当前没有有效测试会话和可用运行证据。
- 完整档案：`docs/10-iterations/2026/09/h5-logout-route-guard/`。

## 头像对象 Key、校验与去重（2026-09-11）

- 新增 V6：用户头像引用从文件 ID 迁移为 `avatar_file_url`，实际保存 `app_file.object_key`，不保存 MinIO Endpoint、完整 URL 或签名参数；旧 READY 头像在删除外键/旧列前回填。
- H5 在上传前以文件头识别实际 JPEG/PNG/GIF/WEBP 类型并计算 SHA-256，不相信扩展名或浏览器声明 MIME；服务端以当前用户范围复用同摘要的 READY 头像，并在确认时流式核对实际大小、SHA-256、声明 MIME 与文件头。
- PASS：`mvn test` 36/36（含同摘要和历史无摘要头像去重单测）、前端页面回归 24/24（含声明 PNG、真实 JPEG 的图片）、类型检查及 H5/微信生产构建。检查编译产物后确认 uni-app H5 会将模板 file input 改为文本输入；个人中心改为在点击手势中动态创建原生 file input，修复点击无响应，H5 已再次构建。NOT_RUN：目标 MySQL/Flyway 与真实 MinIO 上传/回读。BLOCKED：指定 H5 个人中心地址因无登录态被守卫重定向至登录页，未执行写入测试。
- 完整档案：`docs/10-iterations/2026/09/avatar-object-key-dedup/README.md`。

### 头像保存时关联补充（2026-09-12）

- 文件上传完成或命中同图复用时仅成为 READY 文件，不再直接更改当前用户头像。个人中心会暂存新文件 ID；页面预览可以在不改变当前头像引用的前提下立即切换。
- 点击资料保存后，后端在同一资料事务中验证该文件属于当前用户、类型为 AVATAR、状态 READY 且未删除，随后才保存稳定对象 Key。
- PASS：后端单测 37/37，前端页面回归 25/25、类型检查及 H5/微信生产构建。NOT_RUN：真实 MinIO/H5 登录态上传验收尚未执行。

### 个人中心头像立即本地预览（2026-09-12）

- 资料页在文件上传完成后立即采用返回的短时预览 URL 展示新头像，用户无需先保存才能确认选图。
- 选择阶段仅更新页面状态并暂存文件 ID，不调用资料 PUT 或写入当前用户头像引用；点击保存才提交 `avatarFileId`。
- PASS：前端流程回归 26/26、`pnpm run typecheck`、临时注入本地 API 地址后的 H5/微信小程序生产构建均成功。真实 H5/MinIO 登录态上传与资料写入仍为 NOT_RUN，详见 `docs/10-iterations/2026/09/profile-avatar-local-preview/09-verification.md`。

### 个人中心返回“我的”页（2026-09-12）

- 根因：个人中心复用了通用返回函数；页面历史栈只有一层时，该函数按通用兜底切换到首页。
- 修正：顶部返回和资料保存成功确认均使用页面专用返回函数，固定 `switchTab` 到“我的”页；不修改其他页面的通用返回语义。
- PASS：前端流程回归 27/27、`pnpm run typecheck`、临时注入本地 API 地址后的 H5/微信小程序生产构建均成功；真实 H5/微信路由操作仍为 NOT_RUN，详见 `docs/10-iterations/2026/09/profile-back-to-mine/09-verification.md`。

### 个人中心保存成功自动返回（2026-09-12）

- 资料保存成功后只显示项目统一的圆角成功弹层，提示“正在返回我的…”，不再叠加系统 Toast，也不要求用户点击确认按钮。
- 弹层展示 0.5 秒后自动切换到“我的”页；页面卸载时清理未完成定时器。失败流程继续停留当前页并保留既有错误提示。
- PASS：前端流程回归 27/27、`pnpm run typecheck`、临时注入本地 API 地址后的 H5/微信小程序生产构建均成功；真实操作为 NOT_RUN，详见 `docs/10-iterations/2026/09/profile-save-auto-return/09-verification.md`。

## 我的页头像仅展示（2026-09-12）

- “我的”页顶部头像已改为纯展示元素，没有点击响应；不跳转个人中心、不预览，也没有选择或上传照片的路径。设置列表中的个人中心入口及既有资料管理能力保持不变。
- PASS：更新后的前端回归 26/26、类型检查、临时设置本地 API 地址后的 H5/微信小程序生产构建均已通过。NOT_RUN：真实登录态和微信开发者工具验收。
- 完整档案：`docs/10-iterations/2026/09/mine-avatar-preview-only/README.md`。

## 登录注册协议勾选与协议页面（2026-09-12）

- 登录、注册共享认证组件已在图形验证码下方增加协议勾选；计算提交条件和提交函数均要求同意《用户协议》《隐私协议》，避免仅依赖按钮禁用。
- 两份协议新增独立、未登录可读页面，H5 守卫将它们列为公开路径；页面复用关于与帮助的固定导航、渐变引导卡和分组圆角内容卡。
- 隐私协议按当前实际实现说明账号认证、验证码、登录审计、头像和记账数据；明确未将未开放的导出、账单附件、微信认证或广告统计列为现有能力。
- PASS：前端流程回归 28/28、类型检查、H5/微信小程序生产构建。NOT_RUN：真实浏览器与微信开发者工具视觉截图。完整档案：`docs/10-iterations/2026/09/auth-legal-agreements/`。

## 账号字符限制与注册昵称默认值（2026-09-12）

- 登录、注册和个人中心首次账号设置统一为 2–64 位英文字母或数字；前端输入时会去除中文、空格和符号，后端认证与资料服务仍作为最终校验。
- H5 首次注册改为使用规范化后的账号作为昵称，避免新用户仍显示固定“账本主人”。既有账号不自动改写，仍不可在个人中心修改。
- PASS：前端流程回归 29/29、定向后端测试 12/12、后端全量测试 40/40、类型检查和 H5/微信小程序生产构建。NOT_RUN：真实登录、注册与个人中心运行验收。完整档案：`docs/10-iterations/2026/09/account-alphanumeric-nickname-default/`。

## 后端日志目录（2026-09-12）

- 开发 Profile 的文件日志目录为 `D:/github/log/haji`，生产 Profile 为 `/home/hahaen/log/haji`；控制台日志格式、级别和业务逻辑均保持不变。
- PASS：Profile YAML 加载测试和后端全量测试均通过（41/41）。NOT_RUN：真实开发/生产环境的目录创建与文件写入，需在具备依赖服务和生产目录权限后验证。完整档案：`docs/10-iterations/2026/09/backend-log-directories/`。

## H5 静态目录发布权限（2026-09-13）

- 根因：H5 编译已完成，但流水线以 `jenkins-deploy` 用户对 root 所有的 `/home/hahaen/nginx/html` 执行 `install -d`。该命令会尝试设置目录权限，因非所有者而失败，静态产物未被删除或复制。
- 修正：保留既有 ACL 写入模型，将该步骤改为 `test -d` 和 `test -w`，仅校验目录存在与可写，不再尝试改变权限。
- 追加根因：目录预检修复已执行，但 `cp -a` 尝试保留 root 所有目标目录的时间戳，仍以 `Operation not permitted` 退出；改为不保留属性的 `cp -R`。
- PARTIAL：代码差异与空白检查完成；推送后 Jenkins 重新部署、Nginx 重载和公网 H5/API 访问仍待实际验证。完整档案：`docs/10-iterations/2026/09/h5-static-deploy-permission/`。

## 数据库测试初始化基线（2026-09-12）

- 因开发数据库已清空，当前测试阶段所需的资产账户 `sort_order`、头像 `avatar_file_url` 和头像去重索引已直接写入现有基础迁移。
- V1–V4 的业务表均显式使用 `utf8mb4` 和 `utf8mb4_general_ci`；开发与通用 Spring JDBC URL 已同步使用 `characterEncoding=utf8mb4` 和 `connectionCollation=utf8mb4_general_ci`。
- PASS（静态）：当前测试基线的迁移目录、Entity、Service 和数据库文档已完成核对；`MigrationIntegrityTest` 1/1 通过。后续结构调整继续按新版本 Migration 管理。
- NOT_RUN：本机 `information_schema` 当前未发现 `haji_dev`，尚未执行重新建库/建表后的 Flyway 和 `information_schema` 实际验收。
- 完整档案：`docs/10-iterations/2026/09/database-initialization-adjustment/`。

## 2026-09-16 追加：全局 UI 与 H5/微信小程序跨端适配优化

- 共享前端样式完成全局复查，修复认证页小屏纵向滚动受限、按钮/文字平台默认行高差异、弹层底部安全区缺失、固定高度弹层内容滚动边界不完整等问题。
- 变更范围仅为 `app/src/prototype.scss`、`app/src/styles.scss`、布局静态回归 `app/tests/responsive-layout.test.mjs` 和本轮迭代档案；未修改页面脚本、接口调用、方法名、业务逻辑、数据库或后端。
- PASS：布局与既有前端 Node 回归 52/52、`pnpm run typecheck`、H5 构建、微信小程序构建、`git diff --check`；H5 浏览器认证/注册/协议页显示、密码显隐点击和协议长内容滚动通过。
- PARTIAL：H5 实际运行仅覆盖未登录认证/协议页面和 771×1272 浏览器视口，真实登录账务页面、窄屏设备和所有真实数据弹层尚未闭环。
- BLOCKED / NOT_RUN：`pnpm run lint` 因项目无 script 阻塞；本机虽存在微信开发者工具安装文件，但当前 UI 自动化面未暴露可控制的原生窗口，未执行真实小程序页面、安全区、滚动和点击验收。
- 完整档案：`docs/10-iterations/2026/09/global-ui-cross-platform-optimization/README.md`。

## 2026-09-17 追加：微信小程序日历选中日期可见性

- 根因风险：日历日期格使用原生 `button`、日期数字使用 `text`，选中态背景和文字颜色依赖平台差异较大的盒模型；H5 可见不代表微信小程序渲染一致。
- 修正：日历日期格和日期数字改为 `view`，并为日期格、数字补充明确的 flex 盒模型；选中态强制使用 `#49ad9c`、`background-color` 和白色文字，保留当前月选择、非本月不可选、今天态和既有 API/业务逻辑。
- PASS：前端回归 53/53、`pnpm typecheck`、H5/微信小程序生产构建、`git diff --check`，以及小程序 WXML/WXSS 静态产物核对。
- BLOCKED / NOT_RUN：未在微信开发者工具中导入产物并进行真实登录态日历点击/截图，不能把构建结果当作运行时验收。
- 完整档案：`docs/10-iterations/2026/09/calendar-mini-selected-state/README.md`。

## 2026-09-17 追加：微信小程序主题色跨端兼容

共享样式原先大量依赖 CSS 自定义属性 `var(--主题色)`。为避免微信小程序端颜色解析或原生组件层叠失败后回退为黑色，保留现有色板定义，同时将实际颜色声明改为对应的明确十六进制值；PageHeader 作用域样式同步处理。前端回归 54/54、类型检查、H5/微信小程序构建、微信 WXSS 主题色静态检查和 `git diff --check` 为 PASS；真实微信开发者工具页面画面与点击仍为 BLOCKED/NOT_RUN。

详细档案：`docs/10-iterations/2026/09/mini-color-theme-compatibility/`。

## 2026-09-17 追加：新增记账退出不提示放弃修改

- 新增记账的返回路径不再把输入金额、备注、账户选择或类型切换视为需要确认放弃的修改；仅编辑已有账单时比较初始快照并保留自定义确认弹层。
- PASS：前端 Node 回归 55/55、类型检查、H5/微信小程序生产构建和 `git diff --check`。
- NOT_RUN：未在真实登录 H5 或微信开发者工具中操作退出；构建与静态回归不替代运行时验收。
- 完整档案：`docs/10-iterations/2026/09/transaction-create-exit-no-discard/`。

## 2026-09-20 追加：新增与编辑记账页 H5 手机滚动边界

针对上一轮仅增加惯性滚动仍无法覆盖用户手机现象的问题，H5 `.entry-content` 明确设置 `flex:1 1 0`、`height:0`、`min-height:0` 与 `overflow-y:auto`，固定数字键盘和微信小程序样式未改。布局静态回归、TypeScript 和 H5/微信小程序构建已执行；只读 H5 手机尺寸夹具确认主体滚动高度大于可视高度。真实 iOS Safari、真实登录账务数据和微信开发者工具仍未执行，详见 `docs/10-iterations/2026/09/h5-entry-page-scroll/README.md`。

## 2026-09-20 追加：全局按钮与可点击控件轻触反馈

共享样式为按钮、role=button、开关、选择器、链接和资产排序操作增加轻微按压反馈；H5 在浏览器支持时调用轻振动，微信小程序改为各实际页面根节点捕获触摸调用 uni.vibrateShort，不依赖 App slot 冒泡。日历格、资产账户行、未登录昵称和开关补充可识别标记，未改变业务点击回调。前端 Node 回归 60/60、类型检查、H5/微信小程序生产构建为 PASS；H5 移动浏览器和微信开发者工具/真机的真实物理振动仍为 NOT_RUN，详见 docs/10-iterations/2026/09/tap-feedback/README.md。

## 2026-09-28 追加：移除全局点击振动

普通按钮和明确可点击控件不再触发系统振动，`prototype.scss` 的轻微按压缩放保留。H5/微信振动调用已收窄到记账金额键盘的有效按键，禁用按键跳过。前端回归 76/76、TypeScript 检查、H5/微信小程序生产构建及构建产物振动 API 范围核对 PASS；真实移动浏览器和微信工具/真机触摸仍为 NOT_RUN。详见 `docs/10-iterations/2026/09/global-click-vibration-remove/`。

## 2026-09-20 追加：微信小程序转发

- `app/src/utils/wechatShare.ts` 在 `MP-WEIXIN` 条件下注册 `onShareAppMessage`；`pages.json` 全部 14 个页面统一分享固定标题并回到首页，不携带当前路由、账单或用户隐私参数。H5 不注册该生命周期，未新增后端、数据库或 API。
- PASS：转发专测 15/15、H5 生产构建、微信小程序生产构建；微信产物已确认 14/14 页面引用统一转发模块。
- PARTIAL：全量前端 Node 回归 75/77，剩余 2 项为工作区既有 mine 模板断言与当前 tap-feedback 标记不匹配。
- PASS：`pnpm run typecheck`，`vue-tsc --noEmit` exit 0。
- BLOCKED：`app/package.json` 未配置 lint script。
- NOT_RUN：微信开发者工具/真机右上角实际转发及接收方打开首页，需导入 `app/dist/build/mp-weixin` 后验收。
- 完整档案：`docs/10-iterations/2026/09/wechat-mini-share/`。

## 2026-09-20 追加：微信小程序个人中心密码加密兼容

- 根因是个人中心复用了仅浏览器可用的 `crypto.subtle`，微信小程序缺少 Web Crypto 时在请求前抛出“请使用 HTTPS 浏览器”错误。
- 新增微信平台工具层：继续读取既有 SPKI 公钥，使用 RSA-OAEP、SHA-256、MGF1-SHA-256 加密，并只使用 `wx.getRandomValues` 生成 32 字节 OAEP 种子；H5 路径、后端接口和数据库均不变。
- PASS：密码互操作专测 2/2、TypeScript、H5/微信小程序生产构建、构建产物调用链静态核对、后端 53/53。
- PARTIAL：全量前端回归 75/77，2 项为当前 mine 模板与既有断言不匹配；本次未修改该页面或断言。
- BLOCKED：项目无 lint script。
- NOT_RUN：微信开发者工具/真机首次设置及修改密码、真实 DEV API 和数据库 BCrypt 落库验收。
- 完整档案：`docs/10-iterations/2026/09/wechat-mini-password-encryption/`。


## 2026-09-27 追加：本地 dev 连接凭证隔离

`application-dev.yml` 已去除数据库、Redis、MinIO 账号密码默认值，连接参数改为环境变量并允许加载被 Git 忽略的 `server/application-dev.local.yml`；示例文件只包含占位符。静态配置与忽略规则核对为 PASS。`mvn test` 在 Java 17.0.3 环境中因项目要求 Java 25 而于编译阶段 BLOCKED，测试用例未运行；DEV 启动及真实基础设施连接为 NOT_RUN，不能据此宣称运行闭环。详细记录见 `../10-iterations/2026/09/local-dev-credentials/README.md`。

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

详见 [item-lifecycle](../10-iterations/2026/09/item-lifecycle/README.md)。

## 2026-09-28：微信小程序启动白屏修复

开发者工具现场复现 `app.js` 读取 `crypto` 抛错，首页随后未注册。定向修正 `node-forge` 对全局对象的初始化方式后，重启开发构建并在微信开发者工具 2.02.2608070 中重新加载，首页内容实际可见。前端回归 82/82、密码互操作 2/2、TypeScript 与 H5/微信构建为 PASS；真机与真实设置/修改密码为 NOT_RUN。详见 [本轮档案](../10-iterations/2026/09/wechat-mini-startup-crypto/README.md)。

## 2026-09-28：物品清单排序

物品列表查询现先排正在服役，再排已退役；各状态组内购买日期由新到旧，同日按 ID 倒序。排序发生在分页前，保证跨页顺序一致。代码和 API 契约静态核对为 PASS；自动化测试、类型检查、构建、DEV API 与页面运行验收均为 NOT_RUN。详见 [本轮档案](../10-iterations/2026/09/item-list-order/README.md)。

## 2026-09-28：物品详情资料编辑

物品详情新增编辑资料入口，可修改名称、购买价格和购买日期。后端通过用户锁、目标物品归属检查、日期/金额/名称校验及V7幂等关系表保护更新；保持状态和退役资料不变，成本详情随新购买资料重算。静态实现核对、前端类型检查、H5/微信构建、Java 25跳过测试打包及空白检查为PASS；测试用例、V7真实Migration、DEV API及客户端运行验收仍为NOT_RUN。详见 [迭代档案](../10-iterations/2026/09/item-profile-edit/README.md)。

## 2026-09-29：物品日期弹窗年份选择

物品日期范围统一为 `2000-01-01` 至当天，年份栏保留 2000 年起的普通年份列表，通过透明首尾留白允许当前年居中，不显示单独悬浮年份按钮或虚拟窗口。浏览器曾显示年份末项高亮错到 2024，已据此修正滚动几何；修改后自动化点击桥接超时，浏览器最终视觉与实际点选为 PARTIAL/NOT_RUN。前端物品校验、后端 Service 校验和 API 文档保持一致；验证详见 [专项档案](../10-iterations/2026/09/item-date-picker-year/README.md)。

## 2026-09-29：共用日期弹窗滚动与年份范围

用户后续明确年份滚轮需完整显示 2000–2099、默认当天，同时物品未来日期仍不可保存。修复前 Chrome 物品购买日期字段为 2026-09-29，滚轮却高亮 7 月；几何复现进一步确认缺少月/日首尾留白会让 9 月跳至 7 月、1 日跳至 3 日。共用日期/时间组件统一留白与中心换算，物品的 `minDate/maxDate` 在确认时阻止越界值。PASS：修复后几何回归 4/4、前端全量 87/87、TypeScript 检查及 H5/微信构建；H5 实际点选 8 月、27 日并确定后表单回写 2026-08-27，拖动至 12 月及 2099 年后高亮未跳回，未来日期确定时提示范围错误且表单保持当天。未提交物品，随后恢复当天默认值。NOT_RUN：微信开发者工具/真机。详见 [专项档案](../10-iterations/2026/09/date-picker-scroll-stability/README.md)。

## 底部导航前三项图标放大（2026-09-28）

- 首页、日历和资产图标均调整为 `scale(1.6)`，与“我的”使用相同比例；“物品”和“我的”样式保持原样。
- CSS 静态核对为 PASS；H5/微信页面视觉复核、类型检查与构建为 NOT_RUN。

## 底部导航五项图标尺寸统一（2026-09-28）

- 现有 H5 页面显示：原 `scale(1.6)` 的首页、日历、资产、我的明显大于物品图标。移除额外缩放后，经浏览器反馈首页、日历仍偏小而物品偏大；进一步调整字符字号与物品描边框尺寸。截图测量发现首页、日历字形纵向中心低约 2px，已单独上移对齐。
- H5 物品页浏览器画面与底栏局部截图核对 PASS；前端类型检查、H5 与微信小程序构建 PASS。微信开发者工具和真机视觉 NOT_RUN。
- 截图：`app/tests/evidence/bottom-nav-icons-20260928.jpg`。详见 [迭代档案](../10-iterations/2026/09/bottom-nav-icon-size-unification/README.md)。

## 物品清单退役行灰底（2026-09-29）

已退役物品行按 `RETIRED` 状态使用 `#f1f3f2` 灰色底色，在役行继续使用白底。静态实现核对、类型检查、H5 与微信小程序构建为 PASS；带退役物品的真实 H5 页面及微信开发者工具/真机视觉为 NOT_RUN。详见 [迭代档案](../10-iterations/2026/09/item-retired-row-background/README.md)。

## 2026-09-29 追加：H5 与微信小程序页面适配

已在本地已登录 Chrome H5 和微信开发者工具 iPhone 5 / iPhone 15 Pro Max 模拟器中逐页查看五个主页面，并检查记账、物品详情、个人中心、帮助及新增物品弹窗。修复微信品牌页头碰到系统胶囊、矮屏日历记录区不可到达和 H5 横屏金额被键盘完全盖住。H5 的 320×568、375×667、390×844、430×932、667×375 视口检查均无横向溢出；日历矮屏和记账表单的滚动区域可到达。前端回归 87/87、类型检查和双端生产构建 PASS。小程序日历触摸滚动与有数据长列表、微信真机、iOS Safari 和写操作为 PARTIAL/NOT_RUN，详见 [专项档案](../10-iterations/2026/09/responsive-pages-followup/README.md)。

## 2026-09-30：待办清单独立功能

新增“我的”页通知中心下方入口，与账本和记账数据无关联。V10 五表已在 DEV 执行：规则、逐次发生项、渠道投递、每次发送尝试、写请求幂等；各表字符序为 `utf8mb4_general_ci`，发送尝试记录渠道/实际消息/结果/时间，不记录 Key。PASS：Flyway 与 `information_schema`/Entity 核对、双用户真实 HTTP 生命周期与权限、重复边界及模拟发送测试、前端类型检查和 H5/微信构建，以及 Chrome H5 的每隔两月新增、完成推进、编辑历史快照和两类删除。微信开发者工具模拟器的一次性新增、完成、计数与删除 PASS，原生重复选值后的独立持久化验收 PARTIAL。FAIL：全量后端回归中既有日志目录断言 1 项。NOT_RUN：微信真机。BLOCKED：外部通知真实受理和设备送达。整体 PARTIAL。证据与回退见 [待办清单档案](../10-iterations/2026/09/ha-todo/README.md)及[客户端测试报告](../10-iterations/2026/09/ha-todo/11-client-test-report.md)。

08:01–08:23 补测：新增/编辑表单改为独立路由，详情接口限定本人未完成项；H5 和微信模拟器实际验证新建、编辑、未保存提示和保存后清单刷新。H5 硬刷新编辑页的返回缺陷已修正并复测。待办页头在微信模拟器避开安全区，操作按钮居中；统计卡保持原布局。H5 按钮内容几何偏差 0–1 px，微信模拟器目视复查卡片操作按钮，均 PASS（本次视口）。前端类型与双端构建、待办 DEV 定向测试 PASS，其他外部验证边界不变。

## 2026-09-30｜自动提交长期规范

用户授权后续代码修改在必需验证通过、拉取当前分支 upstream、解决冲突并复验后自动执行本地提交，无需再次确认；中文 Conventional Commit 格式为 `<type>(<scope>): <中文摘要>`。规则明确任务范围、暂存审查、验证阻塞和当次不提交指令优先级。PASS：规范与索引同步、本地链接核对、Git 空白检查、两次拉取均 Already up to date。真实冲突处理 NOT_RUN（本次无冲突）；本次纯文档变更不涉及业务测试或构建。

## 2026-09-30 追加：自动提交包含推送

用户补充长期授权：验证、远端同步、冲突处理与复验通过后，自动提交并推送到当前分支 upstream，无需再次确认。AGENTS.md、项目 README、文档中心和测试规范同步更新；禁止强制推送，推送拒绝时重新同步复验，失败必须如实报告并保留本地提交。

本次为纯文档更新（database / api / backend / frontend 无变更），检查内容、链接与 Git 空白；实际同步及提交、推送结果以 Git 命令与交付报告为准，不据规则文字推断已推送。回滚可 revert 本次规范提交并正常推送。

## 2026-09-30｜本地后端端口 9898

PASS：开发后端默认端口及前端 development API 改为 9898；H5 仍为 5180，正式配置无差异。Java 25 打包、DEV 启动、认证公开接口 HTTP 200 与 localhost:5180 跨域预检通过。未进行业务写入；业务全量回归不适用。详情见 docs/10-iterations/2026/09/local-backend-port-9898/README.md。

## 2026-09-30｜物品长期日均成本走势

PASS：物品与坐标定向回归 9/9、前端类型检查、H5/微信生产构建。PASS（只读 H5 夹具）：375px 在役及退役负值曲线、跨年日期；320px 无横向溢出。PARTIAL：示例视觉证据不证明真实登录态。NOT_RUN：微信模拟器与真机视觉。无数据库、后端和业务金额变更。详见 [迭代与截图](../10-iterations/2026/09/item-cost-chart-scale/README.md)。

## 2026-09-30｜待办重复逻辑适配

新增TIME/AFTER_COMPLETION/FIXED_DATES，1–365天/周/月/年、周/月/年度多选和月末；V11对规则与发生项保存完整快照。用户追加要求取消旧预设下拉，规则直接展示，双端共用组件。必需门禁PASS：13项后端定向验证含真实DEV数据库/API，39项前端回归，类型与双端构建、Java25打包、H5/微信模拟器规则控件交互。新代码已在本地9898启动。完整真实用户客户端写入链路PARTIAL，真机/全尺寸NOT_RUN，外部通知送达BLOCKED。详情与截图见[本轮档案](../10-iterations/2026/09/todo-custom-repeat/README.md)。

## 2026-09-30｜待办表单反馈优化

落实八项反馈：标题/计划时间/重复必填星号，重复移除开关且默认按时间每1天，68px居中间隔输入，新建默认提醒开启，备注“选填”无括号，新建直接返回，编辑变更确认复用系统居中按钮，清单隐藏发送记录而数据库审计保留。PASS：41项前端回归、类型及H5/微信构建、实际H5/微信模拟器默认值与返回/确认交互。无数据库/API/后端变更，本轮未启动额外9898服务。真实用户保存与微信真机/全尺寸NOT_RUN（补充验收），外部送达仍BLOCKED。详情见[完整迭代](../10-iterations/2026/09/todo-form-polish/README.md)。

## 2026-09-30｜待办列表与详情整改

PASS：47项前端定向回归、类型检查、H5/微信生产构建；Java25待办13项测试（无跳过，包含真实DEV HTTP+DB权限/历史详情/删除校验）与打包。PASS（H5隔离夹具）：375px圆圈中心偏差0px、暖黄期限与提醒角标、点击内容详情/修改规则/取消返回、圆圈完成刷新与历史详情仅删除、确认删除返回；320/414/812横屏无横向溢出。PASS（微信已测）：我的勾形、待完成/已完成列表、待完成详情与返回。PARTIAL：微信完成历史详情在9898旧服务报错，新接口已通过真实DEV随机端口HTTP验证，但9898未重启，模拟器未复测新接口。NOT_RUN（补充）：微信真机与真实用户完成/删除点击写入；真实用户数据未修改。外部通知送达仍BLOCKED（既有边界）。数据库无迁移。详见[迭代和截图](../10-iterations/2026/09/todo-list-detail/README.md)。

## 2026-09-30｜待办提醒正文前缀与本地诊断

- PASS：Bark/pushplus 正文增加「哈记账： 」；Java 25 定向回归 14 项无失败/错误/跳过，验证 sender 正文与审计快照一致；后端打包通过。无数据库/接口字段/前端变更，前端构建不适用。
- PASS（前缀修改前诊断）：用户授权在本人 DEV 账号创建单次提醒，后台自动扫描并向 pushplus 发送，平台返回受理；此前 23:20 待办在 22:20 已完成，被扫描排除。
- FAIL（外部配置）：Bark 自动请求 HTTP 400，纠正前四次尝试失败；官方只读注册查询返回无法按当前保存值获取设备 Token。保存值为 64 位十六进制，用户确认误填设备 Token，需在通知中心改填 Bark 推送 Key；不回显敏感值。
- NOT_RUN（补充）：手机送达确认与新前缀终端验收；现有 9898 JVM 未重启，新代码需重启本地后端加载。此项不作为已送达证据。

详见 [本次迭代](../10-iterations/2026/09/todo-reminder-prefix/README.md)。

2026-10-01 追加：用户授权后已将本人 DEV Bark 配置纠正为推送 Key，仍以 AES-GCM 加密保存并回读核对；官方只读注册查询 HTTP 200（PASS）。旧值失败记录保留。测试待办 Bark 待重试时间提前后，00:01:23 平台受理，投递为 SENT、最后一次尝试为 ACCEPTED；Bark/pushplus 双渠道平台受理 PASS，手机送达仍待用户确认。

## 2026-10-01｜待办编辑返回失效详情修复

修改规则会替换未完成发生项，原编辑页保存后回旧详情触发“待办不存在，请刷新”。现改为成功回清单并刷新，取消仍回原详情。PASS：先复现失败再修复的实际页面脚本回归、49项前端定向测试、类型检查、H5/微信构建。PARTIAL：页面脚本+API/导航夹具覆盖，未替代真实客户端。NOT_RUN（补充）：真实用户保存、微信工具/真机写入和部署。无后端、Schema、权限或金额实现改动；Java/数据库结构与视觉布局验收不适用。本次必需验证无FAIL/BLOCKED。

详见 [本次迭代](../10-iterations/2026/10/todo-edit-return/README.md)。

## 2026-10-01｜新增页面跨端适配

PASS：108项前端回归、类型检查、H5/微信构建；H5通知中心/待办清单/新增与详情320/375/414及667×375布局无横向溢出；微信430与320模拟器已测清单/新增规则切换及窄屏详情固定底栏。PARTIAL：极限长文本、微信通知页运行及触摸键盘覆盖；NOT_RUN（补充）：真机、iOS Safari、真实保存与通知发送。无后端/数据库/API实现变化，不改写既有送达结论。证据、命令与回退见[本次迭代](../10-iterations/2026/10/new-pages-responsive/README.md)。

## 2026-10-01｜待办页头与通知中心对齐

待办清单、详情、编辑补齐公共20px页顶补偿参数，与通知中心按胶囊对齐。PASS：实际组件计算先复现top=80/预期60，再修复为60；110项回归、类型、H5/微信构建及diff检查。PARTIAL：修复后模拟器画面未核对；NOT_RUN（补充）：微信真机复验。无数据库/API/后端修改。详见[本次迭代](../10-iterations/2026/10/todo-navigation-alignment/README.md)。

## 2026-10-01｜我的页面切回闪烁

修复每次onShow将状态文案替换为加载提示、头像先回默认再加载的问题。同会话保留资料并后台刷新，复用未过期头像地址，处理更新/移除、失败与旧响应隔离。PASS：8项定向回归、118项全量、类型和H5/微信构建；PARTIAL：脚本与模板隔离验收；NOT_RUN（补充）：真实用户页面切换、微信工具/真机及部署。database/api/backend无修改。完整requirement/design/frontend/testing/commands/verification/rollback见[本次迭代](../10-iterations/2026/10/mine-profile-refresh/README.md)。

## 2026-10-01｜四个主导航页面刷新闪烁

首页/日历/资产/物品同会话同条件切回时保留原内容并后台刷新，空状态同样保留；首次及月份/日期/筛选切换仍有反馈，后台失败toast告知，响应序号与会话清理防覆盖。PASS：22项专项、140项全量、类型与双端构建；PARTIAL：实际脚本和模板隔离验收；NOT_RUN（补充）：真实用户切换、微信工具/真机和部署。database/api/backend无变更，完整requirement/design/frontend/testing/commands/verification/rollback见[本轮迭代](../10-iterations/2026/10/tab-background-refresh/README.md)。

## 2026-10-01｜帮助页移除版本文案

- requirement/design/frontend：按用户要求，将帮助页底部“哈记账 · v1.0”改为“哈记账”，同步移除分隔点，保留品牌图标和标语。
- database/api/backend：无变更，相关验证不适用。
- testing/commands：`node --test app/tests/*.test.mjs` 140/140 PASS；`pnpm --dir app run typecheck`、`pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin` 均退出0（PASS）。Python断言核对帮助页源码和微信构建WXML不含v1.0（PASS）。
- verification：上述源码、回归和构建检查PASS；真实客户端画面验收NOT_RUN（补充），无部署操作。
- rollback：将帮助页底部品牌文字恢复为原文案。

## 2026-10-01｜记账日期、滚动与键盘振动

PASS：145项全量回归、类型检查、H5/微信生产构建；本机 H5 新增/普通编辑/还款编辑触摸滚动，键盘固定；微信日期/时间选择二级与一级确认回填；实际 H5 按键振动探针 [15,15] 已恢复。修复 H5 禁滚监听误拦截、微信滚轮祖先拦截与一级弹窗 `.self` 误关、键盘 DOM 依赖。

PARTIAL：硬件震感和所有共享组件调用页；BLOCKED（补充）：本机没有连接可用 Android 真机，手机浏览器尚未确认，微信真机触摸/震感未验；NOT_RUN（补充）：部署、真实账单保存。无必需代码验证 FAIL；无数据库/API/后端变更。

见[完整迭代](../10-iterations/2026/10/entry-touch-interactions/README.md)。


## 2026-10-01｜物品日期点击误关表单

PASS：本机微信工具新增、编辑、退役日期打开、选择、确认回填，一级表单保留；H5生产只读夹具同三入口与背景/关闭按钮取消；146项回归、类型、双端构建及微信WXML事件边界。CenterModal的祖先背景 `.self` 在微信内部点击时误触发关闭，改为独立遮罩与内容兄弟节点，仅H5内容停止触摸冒泡。

PARTIAL：尚无微信手机真机触摸证据；NOT_RUN（补充）：真机、发布及真实物品保存；无数据/API/后端修改，测试草稿均取消。见[完整迭代](../10-iterations/2026/10/item-date-modal-click/README.md)。


## 2026-10-02｜新增与编辑记账跨端滚动

PASS：149项前端回归、类型、H5/微信生产构建、微信编译原生 scroll-view / 无主体 catchtouchmove / 无 disableScroll；工具主体滚轮与账户选择/取消。H5生产只读夹具覆盖4个视口及新增/支出/转账/还款编辑，上滑/回滑、备注可达与键盘固定（高屏无溢出时无需滚动）。

PARTIAL：代码修复完成；原 H5 本机仿真也能滑动，未复现全部手机浏览器触发条件。BLOCKED：无可操作真机，手机 Safari/Chrome 和微信手势验收未完成；真机验收状态保持 BLOCKED；用户在了解该限制后明确要求提交代码，本轮按当次指令提交并推送，不将真机状态改为 PASS。NOT_RUN：部署/发布与真实账单保存。见[本轮记录](../10-iterations/2026/10/entry-native-scroll/README.md)。


## 2026-10-02｜微信全局转发修复

PASS：实际uni-app原生分享生命周期初始化回归、全量130项测试、类型、H5/微信生产构建；微信工具首页/我的/帮助转发入口可用，首页与我的确认卡片标题和固定品牌图正确。工具回调间接注册未生成原生分享方法，改为全局mixin，自动覆盖全部页面；固定首页与品牌图避免携带个人数据与默认截图。H5不注册微信mixin。

PARTIAL：工具只手动抽查3页；NOT_RUN（补充）：真机发送、好友接收打开及线上发布版本。朋友圈当前页分享未开放；无数据库/API/后端变更，无上传发布操作。完整需求、设计、测试、命令、验证和回退见[本轮迭代](../10-iterations/2026/10/wechat-global-share/README.md)。


## 2026-10-02｜固定启动页分享文案图

PASS：用户确认的500×400文案PNG已接入全局分享，约17KB，无图标、插画、账单、余额或当前页面截图；微信工具实际分享框显示正确。132项回归、类型、H5/微信最终构建与打包图片一致性通过。页面加载时将固定图复制到微信本地文件目录，避免工具分享框代码包路径破图；复制失败也指定固定图片。

PARTIAL：工具抽查首页分享卡片。NOT_RUN（补充）：手机发送与接收、上传发布；没有消息发送或用户数据变更。无后端/数据库/API修改。见[本轮记录](../10-iterations/2026/10/wechat-welcome-card/README.md)。


## 2026-10-03｜微信记账备注输入

PASS：134项回归、类型、H5/微信构建、微信DEV编译及两套WXML/WXSS边界；微信工具AX操作新增备注输入保留弹窗、6/100计数、完成回填、重开和取消保留；页面函数覆盖新增空备注及编辑已有备注。备注内容祖先关闭与触摸拦截改为独立兄弟遮罩，内容定位在遮罩上方。

PARTIAL：工具AX未完整复现原始误关症状，根因由模板事件路径推断；真实编辑UI与手机键盘NOT_RUN（补充）。BLOCKED（补充）：原生坐标操作noWindowsAvailable、H5浏览器权限被拒绝。真实账单保存及发布NOT_RUN；后端/数据库/API无变更。详见[本轮迭代](../10-iterations/2026/10/entry-note-input/README.md)。


## 2026-10-03｜微信顶部固定

NativeNavigation 增加统一固定层及等高占位，覆盖现有品牌、欢迎及二级标题页。PASS：类型、134项回归、双端构建和实际组件24组滚动布局。PARTIAL：微信工具页面已加载显示。BLOCKED：自动滚动与拖动返回noWindowsAvailable，随后用户切换工具，未取得微信滚动证据；无可操作真机。按必需平台验证门禁保留未提交修改，未推送或发布。无数据库、API及后端变化，其他并行任务改动排除。见[迭代](../10-iterations/2026/10/wechat-fixed-navigation/README.md)。


## 2026-10-03｜日历主体整体滚动

PASS：134项前端回归、类型检查、H5/微信生产构建、微信WXML唯一原生scroll-view且无catchtouchmove。H5生产只读夹具320×568、375×667、430×932、667×375触摸上滑，月历随主体移动，标题和底栏固定，第12笔完整可见且距底栏25px；五行/六行月历、日期选择、空态、失败重试及月份弹窗通过。取消账单独立高度与740px分支。

PARTIAL：微信生产产物已验证。BLOCKED：微信工具原生窗口操作返回noWindowsAvailable，AX操作后截图与焦点状态未能证明实际滚动；必需微信平台滚动验收未完成，按AGENTS.md暂不提交/推送。NOT_RUN（补充）：手机微信/iOS Safari、部署与发布；无数据库/API/后端及真实账务写入。见[完整迭代](../10-iterations/2026/10/calendar-content-scroll/README.md)。

2026-10-03 电脑控制复测：实际点击进入我的/帮助页；重绑进程、Raise及重置会话后，滚轮仍报noWindowsAvailable，键盘输入无可观察效果。实际微信滚动验收继续BLOCKED，未提交推送。


## 2026-10-03｜用户明确要求提交当前改动

用户本次明确要求“帮我提交代码”，将微信顶部导航固定与日历主体滚动一并纳入提交范围；此前“暂不自动提交”的记录保留为历史状态，本次按明确提交指令执行。微信实际手势验收仍为 BLOCKED，未据此宣称平台运行验收完成。

提交前复验 PASS：`node --test app/tests/*.test.mjs`（134/134）、`pnpm --dir app run typecheck`、`pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin`；设置本机运行库和 Chrome 路径后执行 `node app/tests/fixed-navigation-browser.cjs`（24/24）与 `node app/tests/calendar-scroll-browser.cjs`（4/4视口）。微信生产 WXML 检查：唯一 scroll-view、无 catchtouchmove。浏览器证据使用只读夹具，无真实账务写入；无后端、数据库或 API 变更，未部署或发布。

同步检查 PASS：包含未跟踪文件的 stash 安全备份后，`git pull --ff-only` 返回 Already up to date；恢复后16个文件 SHA-256 全部一致，确认无代码或依赖变化后删除本次 stash。构建与浏览器运行证据对应同一份代码；同步后再次检查回归、类型和差异。


## 2026-10-03｜微信统一10px间距

按用户最终要求全部微信顶栏下方统一10px；补齐品牌和物品详情顶部补偿，并按真实首屏占位坐标校正模拟器安全区偏差。PASS：134项回归、类型、双端构建、本机64组真实组件布局及微信首页/资产初始画面。PARTIAL：未逐页手动抽查；真机及发布NOT_RUN（补充）。无Schema/API/后端变更，无必需阻塞。详见[迭代](../10-iterations/2026/10/wechat-navigation-spacing/README.md)。
