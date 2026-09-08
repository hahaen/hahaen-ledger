# 当前验证矩阵

更新日期：2026-09-08。状态只表示当前工作区实际证据：`PASS`=已执行且符合预期；`PARTIAL`=部分完成；`FAIL`=已执行但不符合预期；`BLOCKED`=外部条件不可得；`NOT_RUN`=尚未执行。

## 当前实现与静态核对

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 文档目录全量清点 | `Get-ChildItem docs -Recurse -File`；当前审计覆盖 `docs/` 下全部文件（含本审计档案新增文件） | PASS |
| 文档相对链接 | 当前审计脚本检查所有 Markdown 相对链接 | PASS |
| 后端域与 Entity/Controller | `server/src/main/java/com/hahaen/ledger/{auth,user,file,account,asset,transaction,home,calendar}` | PASS（代码存在） |
| V1–V5 Migration 文件 | `server/src/main/resources/db/migration/` | PASS（文件静态存在） |
| V3/V4 Entity 与数据库字段 | `AssetAccount`、`TransactionDetail`、`TransactionRefund` 与对应 Migration | PASS（静态核对） |
| 当前用户与逻辑删除过滤 | `CurrentUser`、各 Service/Mapper 查询和删除逻辑 | PARTIAL（静态代码通过；真实跨用户 DB 查询未跑） |
| 账户名称规则 | V3 无数据库唯一索引；`AccountService.assertNameAvailable` 校验有效账户重名 | PARTIAL（存在并发唯一性竞态） |
| 账户余额/欠款直接编辑 | `AccountService.update` 直接更新账户金额，没有余额补齐流水模型 | PARTIAL（缺少可追溯校准流水） |
| H5 认证接口 | `AuthController`、`H5AuthService`、`CaptchaService`、`PasswordCryptoService` | PASS（代码存在） |
| 登录/注册密码显隐控件 | `app/src/components/AuthPage.vue`、`app/src/prototype.scss`；本机 H5 登录/注册页均可点击共享组件右侧 CSS 小眼睛切换掩码/明文 | PASS（页面级验证） |
| 登录/注册页说明文字 | `AuthPage.vue` 已移除微信自动登录与注册资料说明文字，仅保留认证字段和操作入口 | PASS（静态核对） |
| 微信小程序认证 | `app/src/stores/ledger.ts` 调用 `/api/app/auth/login`；当前后端无该路径 | FAIL（接口不匹配） |
| H5 头像链路 | `FileController`、`AppFileService`、`MinioStorageService`、`utils/file.ts` | PARTIAL（代码存在；真实对象上传未在本次执行） |
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
| 顺序字段与旧数据回填 | `V5__add_asset_account_sort_order.sql`；旧有效账户按类型和稳定名称/ID顺序回填 | PASS（静态核对） |
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
