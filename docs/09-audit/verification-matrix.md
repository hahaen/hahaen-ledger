# 当前验证矩阵

更新日期：2026-09-13。状态只表示当前工作区实际证据：`PASS`=已执行且符合预期；`PARTIAL`=部分完成；`FAIL`=已执行但不符合预期；`BLOCKED`=外部条件不可得；`NOT_RUN`=尚未执行。

## 2026-09-13：HTTP 临时认证兼容

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| HTTP 认证密码载荷 | PASS（代码+单测） | 认证页在浏览器不支持 Web Crypto 时，仅在后端显式允许下发送 `compatibilityPassword` 混淆载荷；请求 JSON 不使用明文字段。`H5AuthServiceTest` 4/4 覆盖开关关闭拒绝、HTTP 开启接受、HTTPS 拒绝和既有 RSA 注册。 |
| HTTPS 与个人中心密码保护 | PASS（静态+回归） | HTTPS 继续使用 RSA-OAEP；个人中心继续仅提交 `encryptedPassword`。前端回归 30/30、类型检查和 H5 构建均完成。 |
| 安全边界 | PARTIAL | HTTP 混淆不能防止中间人读取或篡改脚本/载荷，配置默认关闭，仅用于临时注册和登录；部署后必须关闭开关并启用 HTTPS。 |
| 真实 HTTP/HTTPS 认证 | NOT_RUN | 未在服务器上打开开关、重启服务或取得真实浏览器会话验证。 |

## 当前实现与静态核对

## 2026-09-13：Nginx 入口配置说明对齐

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| Jenkins、H5 与 API 路由示例 | PASS（静态） | `deploy/nginx/haji-api-location.conf.example` 已与最终入口统一：`/jenkins/` 保留 Jenkins 前缀，`/haji-api/` 去除公网前缀后访问后端 `/api/...`，其他路径回退 H5 `index.html`。 |
| 真实 Nginx 生效验证 | NOT_RUN | 本次未在服务器执行 `nginx -t`、reload、Jenkins 登录页、H5 路由或 API 请求探针。 |

## 2026-09-12：Jenkins 流水线兼容性修正

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| Gitee 分支参数与 Jenkinsfile 获取 | PARTIAL | `BRANCH=main` 后 Jenkins 已实际获取 `server/Jenkinsfile`；此前参数值为 `origin/main` 时产生重复 `origin/` 前缀，已改正。 |
| Timestamper 依赖 | PARTIAL | 控制台实际报出 `Invalid option type \"timestamps\"`；已从 `server/Jenkinsfile`、`app/Jenkinsfile` 移除非必要 `timestamps()`，待重新提交和构建验证。 |
| SSH、Docker Compose 与服务健康检查 | NOT_RUN | 流水线尚未解析并执行到部署阶段。 |

## 2026-09-12：数据库测试初始化基线

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 当前 V1–V4 迁移目录 | PASS（静态） | V1–V4 保留为当前测试阶段重新初始化数据库的完整结构来源，后续结构变化仍可新增版本迁移。 |
| 资产账户排序结构 | PASS（静态） | `sort_order` 和复合索引已写入当前资产账户建表脚本，新建基线不再执行旧数据回填。 |
| 头像对象 Key 结构 | PASS（静态） | `avatar_file_url` 已写入当前用户建表脚本，头像去重索引已写入文件表建表脚本，旧头像外键逻辑已移除。 |
| utf8mb4 排序规则 | PASS（静态） | V1–V4 显式声明 `utf8mb4_general_ci`；两个 Spring JDBC URL 也已统一。 |
| haji_dev 实际重建 | NOT_RUN | 当前本机 MySQL 的 `information_schema.SCHEMATA` 未发现 `haji_dev`，尚未执行重新建库/建表和 Flyway 验收。 |

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 文档目录全量清点 | `Get-ChildItem docs -Recurse -File`；当前审计覆盖 `docs/` 下全部文件（含本审计档案新增文件） | PASS |
| 文档相对链接 | 当前审计脚本检查所有 Markdown 相对链接 | PASS |
| 后端域与 Entity/Controller | `server/src/main/java/com/hahaen/ledger/{auth,user,file,account,asset,transaction,home,calendar}` | PASS（代码存在） |
| V1–V4 Migration 文件 | `server/src/main/resources/db/migration/` | PASS（文件静态存在） |
| V3/V4 Entity 与数据库字段 | `AssetAccount`、`TransactionDetail`、`TransactionRefund` 与对应 Migration | PASS（静态核对） |
| 当前用户与逻辑删除过滤 | `CurrentUser`、各 Service/Mapper 查询和删除逻辑 | PARTIAL（静态代码通过；真实跨用户 DB 查询未跑） |
| 账户名称规则 | V3 无数据库唯一索引；`AccountService.assertNameAvailable` 校验有效账户重名 | PARTIAL（存在并发唯一性竞态） |
| 账户余额/欠款直接编辑 | `AccountService.update` 直接更新账户金额，没有余额补齐流水模型 | PARTIAL（缺少可追溯校准流水） |
| H5 认证接口 | `AuthController`、`H5AuthService`、`CaptchaService`、`PasswordCryptoService` | PASS（代码存在） |
| 后端 Profile 文件日志目录 | `application-dev.yml` 与 `application-prod.yml` 分别通过 `logging.file.path` 设置开发、生产目录；`LoggingProfileConfigTest` 断言通过 | PARTIAL（真实环境文件写入未执行） |
| H5 与个人中心账号规则 | `AuthPage.vue`、`profile.vue`、`H5AuthService`、`ProfileService`；账号仅 2–64 位英文字母或数字，前端过滤且后端拒绝绕过 | PASS（前后端测试） |
| 首次注册默认昵称 | `H5AuthService.register` 使用规范化账号设置昵称 | PASS（`H5AuthServiceTest`） |
| 登录/注册密码显隐控件 | `app/src/components/AuthPage.vue`、`app/src/prototype.scss`；本机 H5 登录/注册页均可点击共享组件右侧 CSS 小眼睛切换掩码/明文 | PASS（页面级验证） |
| 登录/注册页说明文字 | `AuthPage.vue` 已移除微信自动登录与注册资料说明文字，仅保留认证字段和操作入口 | PASS（静态核对） |
| 登录/注册协议勾选与协议页 | `AuthPage.vue`、`LegalDocumentPage.vue`、`legalDocuments.ts`、`h5AuthGuard.ts`；未勾选不能提交，用户协议/隐私协议可未登录阅读 | PASS（前端流程回归） |
| 微信小程序认证 | `app/src/stores/ledger.ts` 调用 `/api/app/auth/login`；当前后端无该路径 | FAIL（接口不匹配） |
| H5 头像链路 | `FileController`、`AppFileService`、`MinioStorageService`、`utils/file.ts` | PARTIAL（文件头识别实际 JPEG/PNG/WebP/GIF 类型，不信任文件名/MIME；真实对象上传未在本次执行） |
| 账单附件 | Schema 预留 `TRANSACTION_ATTACHMENT`，当前业务页面/Service 未开放 | NOT_RUN |
| 底部导航设计稿对齐 | `app/src/prototype.scss`；本机只读视觉服务资产页 4 个导航项等宽 103px，导航整体和按钮均无边框/圆角/阴影，页面截图核对为连续白色底栏 | PARTIAL（视觉截图未持久化到 `app/tests/evidence/`） |

## 自动化与运行证据

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 后端测试 | `server`: `mvn test`；19 tests，0 failures/errors/skipped，BUILD SUCCESS | PASS |
| 前端类型检查 | `app`: `pnpm run typecheck` exit 0 | PASS |
| H5 生产构建 | `VITE_API_BASE_URL=http://127.0.0.1:8080 pnpm run build:h5` 完成 | PASS |
| 微信小程序生产构建 | `VITE_API_BASE_URL=http://127.0.0.1:8080 pnpm run build:mp-weixin` 完成 | PASS（仅构建） |
| OpenAPI 暴露路径 | `GET http://127.0.0.1:8080/api-docs` 返回 200；包含当前 Controller 路径 | PASS（运行探针） |
| 未登录业务接口 | `GET /api/app/accounts` 返回 HTTP 401 | PASS（运行探针） |
| 验证码接口 | `GET /api/app/auth/captcha` 返回 HTTP 200 | PASS（运行探针） |
| MySQL 端口与 Schema | 当前无 3306 监听；未执行 `flyway_schema_history`/`information_schema` 对照 | BLOCKED |
| Redis 运行与会话 | 当前无 6379 监听；未执行跨重启会话恢复 | BLOCKED |
| MinIO 对象链路 | 当前无 9000 监听；未执行真实上传、确认、预览和删除 | BLOCKED |
| H5 有效会话刷新 | 未取得真实会话；未执行多路由刷新回归 | NOT_RUN |
| 微信开发者工具人工验收 | 未执行 | NOT_RUN |
| 320/375/414 逐区域设计截图 | 当前工作区未找到 `app/tests/evidence/*.png`；仅有视觉服务脚本和日志 | NOT_RUN |

## 2026-09-12 追加：登录注册协议

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 协议勾选、链接与未登录路由 | PASS | `node --test tests/page-flows.test.mjs` 28/28；回归检查认证组件双重提交拦截、协议路由注册和 H5 未登录白名单。 |
| 协议内容与实际处理范围 | PASS | 静态比对 H5 认证、验证码、登录日志、头像对象存储和现有前端能力；正文未将未开放的数据导出、账单附件、微信认证或广告统计写成当前能力。 |
| 类型检查与双端构建 | PASS | `pnpm run typecheck`、设定本地 API 基址后的 H5/微信小程序生产构建均成功。 |
| 320/375/414 与微信开发者工具视觉验收 | NOT_RUN | 本轮未产生真实运行时截图，不能由构建结果替代。 |

## 2026-09-12 追加：账号字符限制与注册昵称默认值

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 登录、注册与个人中心账号校验 | PASS | 前端输入过滤和提交校验、后端两处服务校验统一为 2–64 位英文字母或数字；前端流程回归 29/29。 |
| 后端越过前端保护 | PASS | `H5AuthServiceTest`、`ProfileServiceTest` 覆盖中文、点号、下划线、连字符拒绝；定向 12/12 与全量 40/40 均通过。 |
| 首次注册昵称 | PASS | 注册服务保存规范化账号为用户昵称，后端单测验证 `Demo123` 保存为 `demo123`。 |
| 类型检查与双端构建 | PASS | `pnpm run typecheck` 和注入本地 API 基址后的 H5/微信小程序生产构建通过。 |
| 真实认证与个人中心操作 | NOT_RUN | 未取得真实登录会话和浏览器/微信开发者工具运行证据。 |

## 2026-09-12 追加：头像真实图片类型识别

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 文件名/MIME 与真实内容不一致 | PASS（前端回归） | 文件名及声明为 PNG、JPEG 文件头的样例会申请 `image/jpeg`，与后端文件头校验一致。 |
| 支持格式与安全边界 | PASS（静态+回归） | 仅接受 JPEG、PNG、GIF、WebP 的固定文件头；未知或伪造内容仍拒绝。 |
| 前端回归、类型检查、H5/微信构建 | PASS | `node --test tests/page-flows.test.mjs` 24/24、`pnpm run typecheck`、H5/微信生产构建均完成。 |
| 真实 MinIO 上传 | NOT_RUN | 尚未取得可安全使用的 H5 登录态及可用 MinIO/CORS。 |

## 2026-09-12 追加：头像仅在资料保存后生效

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 上传完成与头像关联 | PASS（单元） | 文件完成和同图复用只返回 READY；不更新 `app_user.avatar_file_url`。 |
| 保存关联的归属校验 | PASS（单元） | `PUT /api/app/user/profile` 的可选 `avatarFileId` 必须属于当前用户、为 AVATAR、READY 且未删除，才在资料事务中保存对象 Key。 |
| 个人中心交互 | PASS（前端回归） | 选择后暂存文件 ID 并立即本地预览；资料 PUT 仍只在保存时发起。我的页顶部头像为纯展示，个人中心仍由设置列表入口进入。 |
| 后端与前端验证 | PASS | Maven 37/37、前端回归 25/25、类型检查、H5/微信生产构建均已执行成功。 |

## 2026-09-12 追加：我的页头像仅展示

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 顶部头像交互 | PASS（前端回归） | 头像没有点击事件；不跳转个人中心、不预览，也不触发选择、上传。 |
| 个人中心入口 | PASS（静态+回归） | 设置列表中的个人中心入口仍保留，资料管理能力未改变。 |
| 前端验证 | PASS | 页面回归 26/26、`pnpm run typecheck`、临时设置本地 API 地址后的 H5/微信小程序构建均通过。 |
| 真实设备预览 | NOT_RUN | 未使用真实登录态或微信开发者工具。 |

完整档案：[mine-avatar-preview-only](../10-iterations/2026/09/mine-avatar-preview-only/README.md)。

## 2026-09-12 追加：个人中心头像立即本地预览

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 选择后立即本地预览 | PASS（前端流程回归） | 页面上传成功后采用返回的短时 `viewUrl`。 |
| 保存前不更新资料 | PASS（前端流程回归） | 选择阶段资料请求列表为空，未发起 `PUT /api/app/user/profile`。 |
| 保存时关联头像 | PASS（前端流程回归） | 仅资料保存请求携带 `avatarFileId`。 |
| TypeScript 与双端构建 | PASS | `pnpm run typecheck` 与临时注入本地 API 地址后的 H5/微信小程序生产构建均成功。 |
| 数据库/Flyway | PASS（范围核对） | 无 Schema、Flyway、Entity 或资料保存事务变更。 |
| 真实 H5/MinIO 登录态 | NOT_RUN | 未执行真实文件上传或资料写入。 |

## 2026-09-12 追加：个人中心返回“我的”页

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 顶部返回与保存成功返回 | PASS（前端流程回归） | 页面只调用 `uni.switchTab({ url: '/pages/mine/mine' })`，不再引用通用返回函数。 |
| 数据库/Flyway | PASS（范围核对） | 无数据库、接口或后端变更。 |
| TypeScript 与双端构建 | PASS | `pnpm run typecheck` 与临时注入本地 API 地址后的 H5/微信小程序生产构建均成功。 |
| 真实 H5/微信路由操作 | NOT_RUN | 未使用真实会话或微信开发者工具。 |

## 2026-09-12 追加：个人中心保存成功自动返回

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 项目成功弹层与自动返回 | PASS（前端流程回归） | 资料保存成功后仅显示项目统一圆角弹层，配置 500ms 后切换到“我的”页。 |
| 成功提示唯一性 | PASS（前端流程回归） | 成功流程未调用系统 Toast，避免重复弹窗。 |
| 保存失败不跳转 | PARTIAL | 既有 catch 保持页面和重试提示；本次未专门注入失败响应。 |
| TypeScript 与双端构建 | PASS | `pnpm run typecheck` 与临时注入本地 API 地址后的 H5/微信小程序生产构建均成功。 |
| 数据库/Flyway | PASS（范围核对） | 无数据库、接口或后端变更。 |
| 真实 H5/微信操作 | NOT_RUN | 未使用真实会话或微信开发者工具。 |

## 2026-09-08 追加：H5 浏览器主应用图标

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| H5 favicon 声明 | `app/index.html` 的 `link[rel="icon"]` 指向 `/static/brand.png`，类型为 `image/png` | PASS |
| H5 开发入口图标资源 | 本机 `http://127.0.0.1:5173/static/brand.png` 返回 HTTP 200 和 `image/png` | PASS |
| H5 生产入口图标资源与透明底 | `app/dist/build/h5/index.html` 含构建指纹 favicon，构建 PNG 四角 alpha 为 0 | PASS |
| 发布环境缓存刷新 | 未部署到真实 CDN/生产域名 | NOT_RUN |

## 当前高风险项

1. 在补齐并验证 `/api/app/auth/login` 前，微信小程序不能作为认证已闭环发布。
2. 账户名称只有 Service 级重复校验，V3 没有数据库唯一约束；并发创建同名账户仍需产品/数据库决策。
3. Migration 文件存在不代表目标环境已经执行；必须在可用 MySQL 上补做 Flyway、`information_schema`、事务和并发验证。
4. `application-prod.yml` 对 H5 RSA 私钥使用生成兜底，部署必须显式注入固定私钥；安全检查不能只依赖默认配置。

## 2026-09-10 追加：退款记录删除失败修复

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 退款记录 DELETE 路径 | PASS（静态核对） | `TransactionController` 暴露 `/api/app/transactions/refunds/{refundId}`，前端调用路径一致 |
| 后端退款软删除与金额回算 | PASS（单元测试） | `TransactionServiceTest.deleteRefundSoftDeletesAndRestoresExpenseImpact` 通过；覆盖退款 `deleted=1`、账单有效金额和支出账户余额 |
| 前端删除后状态 | PASS（回归测试） | `page-flows.test.mjs` 新增场景通过；DELETE 成功后立即移除记录，详情刷新失败不回显旧记录，`saving=false`；详情首次生命周期不发送空账单 ID 请求 |
| 删除失败反馈与影响行数 | PASS（静态核对） | 前端展示后端业务错误；后端 `updateById` 影响行数不是 1 时抛出失败，不误报删除成功 |
| 前端类型检查 | PASS | `pnpm run typecheck` exit 0 |
| H5 生产构建 | PASS | `pnpm run build:h5` 完成 |
| 微信小程序生产构建 | PASS | `pnpm run build:mp-weixin` 完成 |
| Java 测试 | PASS | `mvn test`，26 tests，0 failures/errors/skipped |
| 真实登录态退款删除 | NOT_RUN | 当前未取得有效登录态；需在真实 H5/微信环境验证数据库事务和账户余额 |
| MySQL/Flyway 实际对照 | BLOCKED | 当前无可用 3306 监听，未执行真实库验证 |

## 2026-09-11 追加：累计记账天数按最早记账日统计

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 累计天数统计来源 | PASS（静态+单测） | `ProfileService` 使用当前用户最早 `transaction_detail.occurred_at` 的日期，不再使用 `app_user.created_at`。 |
| 用户隔离与逻辑删除 | PASS（静态核对） | `TransactionDetailMapper.selectEarliestActiveOccurredAt` 限定 `user_id`、`deleted=0`。 |
| 无账单和日期边界 | PASS | 无有效账单或未来最早日期返回 0；最早记账日按自然日含首日计算。`ProfileServiceTest` 3 项通过。 |
| 后端回归 | PASS | `server: mvn test`，29 tests，0 failures/errors/skipped。 |
| Schema/Flyway | PASS（范围核对） | 没有新增或修改 Migration。 |
| 真实个人资料接口与历史账单数据 | NOT_RUN | 未取得有效会话，也未连接 MySQL 执行真实数据复核。 |

## 2026-09-10 追加：首页重复加载请求修复

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| H5 首页首次加载调用链 | PASS（静态核对） | `App.vue` 已不在 H5 启动时调用 `ledger.refresh()`；`index.vue` 自动显示复用同月摘要，未命中时才通过 `onShow -> load -> ledger.refresh()` 加载 |
| 单次刷新请求组成 | PASS（静态核对） | `ledger.refresh()` 的 `Promise.all` 仅各调用一次 `/api/app/home/summary` 与 `/api/app/accounts` |
| 同月并发刷新合并 | PASS（回归测试） | `ledger.ts` 复用相同 `month` 的进行中 Promise；`node --test tests/page-flows.test.mjs` 验证仅产生一条账户和一条摘要请求 |
| 首页重复加载回归测试 | PASS | `app`: `node --test tests/page-flows.test.mjs` 17/17；包含 H5 启动不预取、首页同月摘要复用和同月并发刷新合并断言 |
| 前端类型检查 | PASS | `app`: `pnpm run typecheck` exit 0 |
| H5 生产构建 | PASS | 临时注入本地 API 地址后 `pnpm run build:h5` exit 0；仅有既有 Sass legacy-js-api 弃用警告 |
| 有效会话 Network 刷新验收 | NOT_RUN | 当前本机浏览器未取得可用测试会话，未将静态核对替代为运行时证据 |

## 2026-09-11 追加：首页最近记账双月分段加载

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 首段范围与后续游标 | PASS | `HomeService.recentTransactions` 未传游标时查询当前月和上月；传入 `beforeMonth` 作为排他上界，连续返回更早两个月。`HomeServiceTest` 覆盖 `2026-09` 游标得到 `2026-07` 至 `2026-08`。 |
| 查询归属与逻辑删除 | PASS（静态核对） | `TransactionDetailMapper.selectByPeriod` 与 `existsActiveBefore` 均限制 `user_id`、`deleted=0`；用户 ID 由 `CurrentUser` 获取。 |
| 首页触底追加 | PASS | `page-flows.test.mjs` 覆盖首段请求、使用 `startMonth` 请求下一段、ID 去重和 `hasMore=false` 停止，18/18 通过。 |
| 后端回归 | PASS | `server`: `mvn test`，27 tests，0 failures/errors/skipped。 |
| 前端类型检查与构建 | PASS | `pnpm run typecheck`、H5/微信小程序生产构建完成；构建有既有 Sass legacy-js-api 弃用警告。 |
| 真实登录态滑动联调 | NOT_RUN | 未取得有效测试会话，未执行网络面板和实际账单分页验证。 |

## 2026-09-10 追加：退款逻辑删除标识修复

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 真实退款行根因 | PASS | 开发 MySQL 只读核对目标退款：`deleted_at` 已设置而 `deleted=0`，因此 `selectActiveByTransaction` 的 `deleted=0` 条件仍返回该行；库内同类不一致退款共 3 条。 |
| 退款条件软删除 SQL | PASS（静态核对） | `TransactionRefundMapper.softDeleteById` 显式写入 `deleted=1` 及删除/更新审计字段，并以 `id AND deleted=0` 防止并发重复处理。 |
| 账单级联退款删除 | PASS（静态核对） | `TransactionService.delete` 与 `deleteRefund` 均改用同一条件软删除方法。 |
| 最后一笔退款后的交易标记 | PASS（单元测试） | `deleteRefund` 依据剩余有效退款回算 `hasRefund`；累计退款为 0 时置 0，交易行不显示“退”。 |
| 后端回归 | PASS | `server`: `mvn test`，26 tests，0 failures/errors/skipped。 |
| 已运行 8080 的真实 DELETE 复测 | NOT_RUN | 运行实例在修复编译前启动，未擅自重启或对用户账务重试写操作。 |


## 2026-09-08 追加：资产新增弹窗设计对齐

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 资产页新增弹窗 | `app/src/pages/assets/assets.vue` 已由类型选择弹窗调整为新增资产账户表单弹窗 | PASS（静态核对） |
| 旧新增页面跳转清理 | `account.vue` 新增模式已移除，记账页不再使用 `account/account?kind=...`；账户详情路由保留 | PASS（静态核对） |
| 弹窗视觉样式 | `app/src/prototype.scss` 已增加动态标题、铺满且缩小的类型 Tab、表单行、弱化占位文字和居中操作按钮，金额输入不附加钱币图标 | PARTIAL（未完成有效会话截图复核） |
| 账户创建接口与刷新 | 复用 `useLedger().createAccount` 和现有 `POST /api/app/accounts` | PASS（静态核对） |
| 数据库/后端/API | 本次未修改 | NOT_RUN |
| 前端类型检查 | `app` 执行 `pnpm typecheck`，exit 0 | PASS |
| H5 生产构建 | 临时设置本地 `VITE_API_BASE_URL` 后执行 `pnpm build:h5`，输出 `DONE Build complete.` | PASS |

## 2026-09-08 追加：资产账户顺序

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 顺序字段与初始化基线 | `V3__create_asset_account_table.sql`；`sort_order` 直接建表并默认 `0` | PASS（静态核对） |
| 新建账户默认末尾 | `AccountService.create` 使用同类最大 `sort_order + 1` | PASS（静态核对） |
| 同类账户换序接口 | `AccountController`、`AccountService.reorder`；当前用户、逻辑删除、类型和顺序冲突均校验 | PASS（静态核对） |
| 长按换序交互 | `assets.vue` 的 `@longpress`、类型隔离显示和成功刷新逻辑 | PASS（静态核对） |
| 换序交互视觉微调 | 移除顶部提示条；按长按类型隔离“交换”；`reorder-label` 缩小并提高对比度 | PASS（静态核对） |
| 不计入净资产角标 | `assets.vue` 在两类账户名称右侧按 `includedInNetAsset` 条件渲染“不计入” | PASS（静态核对） |
| 后端换序单元测试 | `AccountServiceTest` 覆盖交换和重复请求不反转；`mvn test` 19 tests 通过 | PASS |
| 前端类型检查 | `app`: `pnpm run typecheck` exit 0 | PASS |
| H5 生产构建 | `app`: `VITE_API_BASE_URL=http://127.0.0.1:8080 pnpm run build:h5` 完成 | PASS |
| 微信小程序生产构建 | `app`: `VITE_API_BASE_URL=http://127.0.0.1:8080 pnpm run build:mp-weixin` 完成 | PASS（仅构建） |
| Flyway/MySQL 实际执行 | 当前无 3306 监听，未对照 `flyway_schema_history`/`information_schema` | BLOCKED |

## 2026-09-08 追加：账户 ID 精度修复

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 账户/流水 ID API 序列化 | `AccountVO`、`TransactionVO`、`RefundVO` 及相关认证、资料、文件 VO 改为字符串 ID | PASS（静态核对） |
| 前端路由和实体类型 | `app/src/utils/id.ts`；账户、账单详情、记账编辑及全局 Store 使用 string ID | PASS（静态核对） |
| 账户点击回归根因测试 | `AccountServiceTest`、`TransactionServiceTest` 增加字符串 ID 断言 | PASS |
| 前端类型检查 | `app` 执行 `pnpm typecheck`，exit 0 | PASS |
| 后端单元测试 | `server` 执行 `mvn test`，19 tests 全部通过 | PASS |
| 数据库结构/金额计算 | 本次未修改 | NOT_RUN |
| 有效登录态浏览器点击验收 | 未取得有效会话截图证据 | NOT_RUN |

## 2026-09-08 追加：账户详情布局

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 详情页顶部空间 | `account.vue` 与 `.account-page .screen-nav` 局部紧凑样式 | PASS（静态核对） |
| 删除账号 | 展示文案改为“删除账号”，仍调用既有逻辑删除接口 | PASS（静态核对） |
| 编辑弹窗 | 复用新增弹窗样式，移除类型 Tab | PASS（静态核对） |
| 前端类型检查 | `app`: `pnpm typecheck` exit 0 | PASS |
| H5 生产构建 | 临时设置本地 API 地址后 `pnpm build:h5` 完成 | PASS |
| 有效会话视觉验收 | 未取得有效登录态截图证据 | NOT_RUN |

## 2026-09-08 追加：前端金额展示统一

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 公共金额格式化 | app/src/utils/money.ts 的 formatYuan() 统一生成不带符号的文本；.00 移除，其余有效小数保留两位 | PASS（静态核对） |
| 金额场景覆盖 | 首页、日历、资产、账户详情、账单详情、退款、还款、交易列表和记账编辑/计算均已搜索并统一 | PASS（静态核对） |
| 空值和类型 | 数字、数字字符串、null、undefined、非法值均有安全结果；边界样例实测通过 | PASS |
| 原金额与接口 | 仅修改前端格式化和输入初始展示；后端、API、数据库和金额计算未修改 | PASS |
| 前端类型检查 | app: pnpm run typecheck exit 0 | PASS |
| H5 生产构建 | 临时本地 VITE_API_BASE_URL 后 pnpm run build:h5 完成 | PASS |
| 微信小程序生产构建 | 临时本地 VITE_API_BASE_URL 后 pnpm run build:mp-weixin 完成 | PASS |
| 真实会话视觉验收 | 未取得有效登录态截图及微信开发者工具人工证据 | NOT_RUN |

## 2026-09-08 追加：还款弹窗细节

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 金额货币符号 | 运行时金额展示和金额输入框均不再渲染 `¥` 或其他钱币图标 | PASS（静态核对） |
| 账户选择弹窗 | 标题缩小，账户列表固定高度并支持滚动 | PASS（静态核对） |
| 必填校验 | 还款账户和还款金额在确认前校验 | PASS（静态核对） |
| 默认选择状态 | 每次打开还款弹窗清空 `repayFundId` | PASS（静态核对） |

## 2026-09-08 追加：还款弹窗与账户选择

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 还款主弹窗 | `account.vue` 自定义还款弹窗，包含欠款、账户、金额和操作按钮 | PASS（静态核对） |
| 还款账户选择 | 展示有效资金账户和余额，选择后返回还款弹窗 | PASS（静态核对） |
| 还款业务接口 | 沿用既有还款接口、金额校验和幂等键 | PASS（静态核对） |
| 前端类型检查 | `app`: `pnpm typecheck` exit 0 | PASS |
| H5 生产构建 | 临时设置本地 API 地址后 `pnpm build:h5` 完成 | PASS |
| 有效会话视觉验收 | 未取得有效登录态截图证据 | NOT_RUN |

## 2026-09-08 追加：金额图标移除

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 公共金额展示组件 | app/src/components/MoneyDisplay.vue 统一只渲染金额数值和必要前缀，不渲染钱币图标 | PASS |
| 金额页面覆盖 | 首页、日历、资产、账户详情、账单详情、退款、还款、记账和公共交易行 | PASS（静态核对） |
| 小屏响应式 | 只读视觉服务在 320/375/414px 核对，三种宽度均无横向溢出，金额无图标换行或错位 | PASS |
| 间距和基线 | 公共 flex 布局统一金额数值的基线和不同字号下的显示效果，不再保留图标间距 | PASS（静态核对） |
| 金额输入 | 资产、账户、还款和记账输入框不再附加 `¥`，不影响提交值 | PASS（静态核对） |
| 前端类型检查 | app: pnpm run typecheck exit 0 | PASS |
| H5 生产构建 | 临时本地 API 地址后 pnpm run build:h5 完成 | PASS |
| 微信小程序生产构建 | 临时本地 API 地址后 pnpm run build:mp-weixin 完成 | PASS（仅构建） |
| 业务逻辑/API/数据库 | 本次未修改金额计算、接口、数据结构和后端 | PASS（范围核对） |
| 真实登录态/微信工具视觉验收 | 当前未取得真实会话和微信开发者工具 | NOT_RUN |
| 静态原型目录 | 项目规范定义为独立设计参考，不属于 app 构建入口，本次未修改 | NOT_RUN（范围外） |

## 2026-09-08 追加：删除账户确认弹窗

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 删除确认弹窗视觉 | `account.vue` 自定义圆角弹窗与 `account-delete-*` 样式 | PASS（静态核对） |
| 逻辑删除行为 | 确认按钮继续调用 `ledger.deleteAccount` | PASS（静态核对） |
| 有效会话视觉验收 | 未取得有效登录态截图证据 | NOT_RUN |

## 2026-09-08 核心页面原型与流程修复

| 项目 | 状态 | 证据/限制 |
| --- | --- | --- |
| 前端金额、日期及保存/竞态回归 | PASS | 9 项 Node tests |
| 后端交易/退款回归 | PASS | mvn test 24 项，交易 9 项 |
| 类型检查、H5/微信构建 | PASS | 临时本地 API 地址，非生产部署 |
| 页面截图、320/414 横向溢出 | PASS | core-pages-prototype/evidence；独立只读测试数据 |
| 资产和我的页面保护 | PASS | git diff 两页面无输出 |
| 真实核心账务闭环 | PARTIAL | 代码/单测完成，认证后的端到端未验证 |
| 登录后真实账务操作 | BLOCKED | 无可用测试登录会话/账号 |
| MySQL 并发、Flyway、Redis、MinIO、微信工具 | NOT_RUN | 不用构建或 Mockito 代替 |

详情见 [迭代记录](../10-iterations/2026/09/core-pages-prototype/README.md)。最终已执行验证无 FAIL 项。

## V5 启动校验修复（2026-09-08）

PASS：移除 V5 首行误加的 cd，恢复数据库既有 checksum 266153221；mvn test package 25 项测试通过。实际 DEV 启动验证 5 个迁移全部通过、Schema 保持 V5 且无需迁移、应用成功启动；临时 18080 未登录账户接口返回 401，验证后停止实例。未执行 repair 或修改数据库历史。记录：docs/10-iterations/2026/09/flyway-v5-checksum/README.md。

## 账单详情顶部对齐（2026-09-09）

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 顶部导航规格 | `app/src/prototype.scss` 将 `.detail-page .screen-nav` 纳入新增记账共用的紧凑导航选择器组 | PASS |
| 类型检查 | `app`: `pnpm run typecheck` exit 0 | PASS |
| H5 生产构建 | 临时注入本地 API 地址后 `pnpm run build:h5` exit 0 | PASS |
| 只读视觉夹具 | 详情页与新增记账页均显示紧凑页头 | PASS |
| 真实登录态验收 | 原开发地址被认证页拦截，未取得有效会话 | BLOCKED |

## 账单详情删除弹窗样式统一（2026-09-09）

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 账单删除确认弹窗 | `app/src/pages/detail/detail.vue` 复用账户详情页圆角弹层结构与危险确认按钮 | PASS（静态核对） |
| 退款删除确认弹窗 | 账单详情页退款记录删除复用同一确认弹层，并保留原退款 DELETE 接口 | PASS（静态核对） |
| 删除业务/API/数据库 | 本次未修改既有删除接口、后端和数据库 | PASS（范围核对） |
| 前端类型检查 | `app`: `pnpm run typecheck` exit 0 | PASS |
| H5 生产构建 | 临时注入本地 API 地址后 `pnpm run build:h5` 完成 | PASS |
| 微信小程序生产构建 | 临时注入本地 API 地址后 `pnpm run build:mp-weixin` 完成 | PASS（仅构建） |
| 只读视觉夹具交互 | 打开账单详情删除弹层并点击取消，未执行写入 | PASS |
| 有效登录态视觉验收 | 未取得有效会话截图 | NOT_RUN |

## 首页交互修正（2026-09-08）

首页“当前年/月 · 日均消费”为普通文字，不提供月份选择；最近记账右侧不显示刷新按钮。年月与查询范围在加载时按当前日期更新。类型检查、H5 构建 PASS。档案：docs/10-iterations/2026/09/home-static-month/README.md。

首页摘要布局修正（2026-09-09）：PASS。日均消费金额已覆盖金额组件的 `inline-flex`，在标题下方独占一行；本月支出/收入按内容宽度排列，避免两列被拉得过远；月份仍由 `localDateTime()` 动态生成并传入首页摘要接口。类型检查、H5 构建和独立只读视觉核对通过。档案：docs/10-iterations/2026/09/home-summary-layout-current-month/README.md。

首页最近记账设计稿对齐（2026-09-09）：PASS。日期标题恢复设计稿边距和字号，日期汇总保持单行右对齐；最近记账滚动容器、内容层和账单行统一为 `#f9fcfb` 浅薄荷底色。类型检查、H5 构建和独立只读视觉核对通过。档案：docs/10-iterations/2026/09/home-recent-list-design-alignment/README.md。

## 2026-09-09 追加：账单详情退款弹窗设计对齐

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 退款弹层结构 | `app/src/pages/detail/detail.vue` 使用退款专用弹层，包含把手、说明、金额输入和取消/确认双按钮 | PASS（静态核对） |
| 系统视觉样式 | `app/src/prototype.scss` 使用现有 30px 圆角、薄荷绿确认色、浅色取消色、阴影和安全区规则 | PASS（静态核对） |
| 默认退款金额 | `openRefund()` 回填 `inputYuan(effectiveCents)`，避免千分位文本直接提交 | PASS（静态核对） |
| 退款后详情金额 | 主金额显示 `effectiveCents`，不再重复渲染“当前有效金额”行 | PASS（静态核对） |
| 退款记录区 | 增加 `REFUND RECORD` 英文眉标题，并按系统暖色退款卡片展示累计退款和明细 | PASS（静态核对） |
| 退款业务/API/数据库 | 本次未修改退款接口、整数分、幂等、后端、数据库 | PASS（范围核对） |
| 前端回归测试 | `node --test tests/entry.test.mjs tests/page-flows.test.mjs`，15/15 | PASS |
| 前端类型检查 | `pnpm run typecheck` exit 0 | PASS |
| H5/微信小程序生产构建 | 两个构建均输出 `DONE Build complete.` | PASS |
| 本机视觉夹具 | Codex 内置浏览器访问本机服务返回 `ERR_BLOCKED_BY_CLIENT` | BLOCKED |
| 真实登录态退款验收 | 未取得有效登录态，未执行真实退款写入 | NOT_RUN |
## 编辑记账类型固定（2026-09-09）

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 编辑态类型回填与固定 | `app/src/pages/entry/entry.vue` 的 `loadForEdit()` 回填类型，`setType()` 拒绝编辑态切换 | PASS（静态核对） |
| 编辑态类型切换器 | 模板仅在 `!isEdit` 时渲染 `.entry-type` | PASS（静态核对） |
| 新增态类型切换 | 新增态 `setType('TRANSFER')` 回归用例通过 | PASS |
| 前端回归 | `node --test tests/page-flows.test.mjs tests/entry.test.mjs`，15/15 passed | PASS |
| 前端类型检查 | `app`: `pnpm run typecheck` exit 0 | PASS |
| H5 生产构建 | 临时注入本地 API 地址后 `pnpm run build:h5` exit 0 | PASS |
| 微信小程序生产构建 | 临时注入本地 API 地址后 `pnpm run build:mp-weixin` exit 0 | PASS |
| 真实详情到编辑操作 | 未取得有效登录账单会话及微信开发者工具证据 | BLOCKED |

编辑页类型提示补充：编辑数据加载成功后，在标题下方显示“当前账单类型：{类型}记账”；15/15 回归、类型检查、H5 和微信小程序构建均通过，真实登录态操作仍为 BLOCKED。

还款类型提示样式补充：编辑页还款类型提示底色统一为既有还款样式的 `#fff4ed`，文字使用既有负债色。

放弃修改弹窗补充：编辑页已移除平台原生 `uni.showModal`，改用系统统一的自定义圆角确认弹层，并保留未保存内容保护逻辑。

## 2026-09-10 追加：日历页固定上半部分与账单独立滚动

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 日历上半部分固定 | `app/src/prototype.scss`：`.calendar-page` 使用 `height:100vh`、`overflow:hidden`；页头、月历卡片和日期标题 `flex-shrink:0` | PASS（静态核对） |
| 账单记录独立滚动 | `app/src/pages/calendar/calendar.vue` 使用 `scroll-view scroll-y`；`.calendar-transaction-list` 使用 `flex:1;height:0;min-height:0;overflow-y:auto` | PASS（静态核对） |
| 布局回归测试 | `node --test tests/calendar-layout.test.mjs tests/page-flows.test.mjs tests/entry.test.mjs`，19/19 | PASS |
| 前端类型检查 | `app`: `pnpm run typecheck` exit 0 | PASS |
| H5 生产构建 | 临时注入本地 API 地址后 `pnpm run build:h5` 输出 `DONE Build complete.` | PASS |
| 独立只读运行时滚动 | 页面 `scrollTop=0`；记录滚动节点 `scrollTop=159.33`，日历页上半部分未移动 | PASS |
| PNG 截图证据 | 本次未生成可提交截图文件 | NOT_RUN |
| 微信开发者工具人工验收 | 本次未执行 | NOT_RUN |

## 2026-09-11 追加：H5 退出登录后的路由访问限制

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 登录/注册公开白名单 | PASS | `app/src/utils/h5AuthGuard.ts` 与 `auth-guard.test.mjs` 覆盖登录页、注册页和业务页路径识别。 |
| 无会话业务路由拦截 | PASS | 守卫监听 hash、popstate、pageshow；28/28 前端回归通过。 |
| 手动退出后的 H5 跳转 | PASS（静态+回归） | `mine.vue` 在 H5 注销清理后 `reLaunch` 到登录页。 |
| 微信小程序兼容 | PASS（构建） | 微信小程序生产构建通过，H5 守卫由条件编译隔离。 |
| 前端类型检查与双端构建 | PASS | `vue-tsc`、H5 和微信小程序生产构建均 exit 0。 |
| 真实 H5 登录态路由验收 | NOT_RUN | 未取得有效会话及浏览器运行证据。 |

## 2026-09-11 追加：我的页退出登录弹窗设计对齐

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 自定义确认弹层 | PASS | `app/src/pages/mine/mine.vue` 已替换 `uni.showModal`，复用系统圆角面板、遮罩、提示条与取消/危险确认按钮。 |
| 注销与重复提交保护 | PASS | `node --test tests/page-flows.test.mjs` 的新增用例验证确认后仅调用一次 `logout()`，完成后关闭弹层。 |
| 前端回归、类型检查和双端构建 | PASS | 页面回归 19/19，`pnpm run typecheck`、H5 和微信小程序生产构建均 exit 0。 |
| 只读运行时视觉验收 | BLOCKED | 内置浏览器访问本机视觉夹具返回 `ERR_BLOCKED_BY_CLIENT`，未生成截图。 |
| 真实登录态注销 | NOT_RUN | 未在真实会话下执行注销。 |

## 2026-09-11 追加：我的页个人中心设置行对齐

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 三项设置行视觉一致性 | PASS（静态核对） | 个人中心、关于与帮助、退出登录均使用 `button.setting-item` 和公共图标样式，避免平台组件渲染差异。 |
| 关于与帮助与退出登录入口 | PASS（静态核对） | 关于与帮助继续调用 `openHelp`；退出登录继续使用 `logout-item`、`openLogout` 及既有确认流程。 |
| 前端页面回归 | PASS | `node --test tests/page-flows.test.mjs`，20 tests passed；新增断言同时确认关于与帮助和退出登录的既有入口。 |
| TypeScript 类型检查 | PASS | `pnpm run typecheck`，exit 0。 |

## 2026-09-13 追加：H5 静态目录部署权限

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| H5 编译与产物检查 | PASS（Jenkins 构建日志） | `pnpm install`、类型检查和 `uni build -p h5 --mode production` 均完成；`dist/build/h5` 存在检查通过。 |
| Nginx 静态目录发布 | PARTIAL | 首次 `install -d` 失败已改为目录存在/可写检查；后续构建越过该步骤，但 `cp -a` 又因尝试保留 root 所有目标目录时间戳失败，现改为 `cp -R`。 |
| 修复后端到端 H5 发布 | NOT_RUN | 尚待 Jenkins 拉取 `cp -R` 修复后重新构建，验证复制、Nginx 重载与公网访问。 |

## 2026-09-11 追加：个人中心资料与密码管理

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 个人中心页面与导航 | PASS（静态+视觉夹具） | `pages/profile/profile.vue` 已注册；我的页入口受登录态保护；主页面复用帮助页导航规格。 |
| 头像、昵称、账号和密码 | PASS（静态+单测） | 保存资料时头像/昵称/账号均前端必填；未设置密码时密码框必填并随资料请求加密提交；`ProfileService` 使用当前 Sa-Token 用户；昵称/账号/密码均双端校验；已有账号不可修改；首次账号与密码同事务，唯一性由预检和现有 `uk_app_user_login_account` 保护。 |
| 密码立即更新和保存成功返回 | PASS（前端流程） | `page-flows.test.mjs` 覆盖密码确认即时 PUT、资料 PUT 成功后显示返回我的弹层。 |
| 头像 | PARTIAL | 新页复用既有 H5 上传与预览；未连接真实 MinIO，微信头像选择仍未开放。 |
| 后端回归 | PASS | `server: mvn test`，34 tests，0 failures/errors/skipped。 |
| 前端回归、类型检查与双端构建 | PASS | 21 项 Node 测试、`pnpm run typecheck`、H5 和微信小程序构建均成功。 |
| 只读视觉验收 | PASS | 本机只读夹具 `/#/pages/profile/profile` 核对主页面和修改密码弹层；未提交写入。 |
| Schema/Flyway | PASS（范围核对） | 复用 `app_user` 既有字段和唯一索引，没有 Migration 变更。 |
| 真实登录态资料/密码写入、MinIO 上传、微信工具 | NOT_RUN | 未取得用于安全写入验证的真实会话，未将单测或夹具替代为真实环境证据。 |

## 2026-09-11 追加：头像对象 Key、校验与去重

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 用户头像引用 | PASS（静态） | V1 直接创建只保存对象 Key 的 `avatar_file_url`；不保存可变 MinIO URL 前缀。 |
| 上传内容校验和同图复用 | PASS（代码+单测） | 前端计算 SHA-256；后端核验实际对象大小、摘要、MIME/文件头；`AppFileServiceTest` 覆盖 READY 同摘要和无摘要历史当前头像均不插入新对象元数据。 |
| 后端、前端与双端构建 | PASS | `mvn test` 36/36、页面回归 23/23、类型检查和 H5/微信生产构建均执行成功；H5 选择器修复后已再次构建。 |
| H5 选择头像 | PASS（单元+构建） | 编译产物确认模板 file input 会被 uni-app 改写为文本输入；个人中心在点击手势中动态创建原生 file input，回归测试覆盖。 |
| Flyway/MySQL 与真实 MinIO | NOT_RUN | 未在目标环境查询 schema/history 或执行对象 PUT、回读和同图复用。 |
| H5 个人中心真实上传 | BLOCKED | 指定地址被无会话路由守卫重定向至登录页；没有可安全使用的登录态。 |

完整档案：[avatar-object-key-dedup](../10-iterations/2026/09/avatar-object-key-dedup/README.md)。

## 2026-09-10 追加：账户详情固定筛选与流水独立滚动

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 固定账户详情头部 | `app/src/prototype.scss`：`.account-page` 使用 `height:100vh`、`overflow:hidden`；导航和账户卡片 `flex-shrink:0` | PASS（静态核对） |
| 固定五项筛选按钮 | `app/src/pages/account/account.vue`：`.filter-row` 位于 `.account-record-list` 之前；筛选查询逻辑未改变 | PASS（静态核对） |
| 日期/笔数与流水独立滚动 | `.account-record-list` 包含日期分组、笔数和 `TransactionRow`，使用 `scroll-view scroll-y` | PASS（静态核对） |
| 日期分组标题吸顶 | `app/src/prototype.scss`：`.account-record-list .date-heading` 使用 `position:sticky;top:0;z-index:2` | PASS（静态核对） |
| 布局回归测试 | `node --test tests/account-layout.test.mjs tests/calendar-layout.test.mjs tests/page-flows.test.mjs tests/entry.test.mjs`，20/20 | PASS |
| 前端类型检查 | `app`: `pnpm run typecheck` exit 0 | PASS |
| H5 生产构建 | 临时注入本地 API 地址后 `pnpm run build:h5` 输出 `DONE Build complete.` | PASS |
| 独立只读运行时滚动 | 页面 `scrollTop=0`；资金账户流水滚动节点 `scrollTop=47.33`，账户卡片和筛选按钮未移动 | PASS |
| PNG 截图证据 | 本次未生成可提交截图文件 | NOT_RUN |
| 微信开发者工具人工验收 | 本次未执行 | NOT_RUN |
## 2026-09-13 追加：H5 已登录默认入口回首页

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 已恢复会话的登录/注册入口 | PASS（路由单测） | `auth-guard.test.mjs` 5/5 通过，覆盖根路径、登录页和注册页的首页分流；类型检查与 H5 生产构建均 exit 0。 |
| 业务页与协议页路由保留 | PASS（路由单测） | 测试确认首页与隐私协议页不被首页分流，继续保留原 hash 路由。 |
| 真实 H5 关闭后重新打开 | NOT_RUN | 需要有效 H5 会话及浏览器操作证据。 |

完整档案：[h5-authenticated-entry-home](../10-iterations/2026/09/h5-authenticated-entry-home/README.md)。
