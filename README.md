# 哈记账

哈记账是面向微信小程序、同时保留 H5 扩展能力的单体全栈记账项目。首版实现单用户、单账本、人民币场景，覆盖支出、收入、转账、退款、信贷还款、账户资产和按月/按日查看。

## 目录

- `server/`：Java 25 + Spring Boot 3.5 + MyBatis-Plus 后端。
- `app/`：Vue 3 + TypeScript + uni-app 用户端。
- `docs/`：架构、数据库、API 和开发约束。
- `sql/`：只放人工检查或运维 SQL，正式 Schema 以 Flyway 为准。

## 环境

- Java 25、Maven 3.9+（本机 Maven 3.6.3 也可构建）
- Node.js 22、pnpm 11
- MySQL 8、Redis 7.4、MinIO
- 微信开发者工具（小程序构建产物导入 `app/dist/build/mp-weixin`）

当前仓库使用 `server/src/main/resources/application-dev.yml` 作为 `dev` Profile 的安全默认配置，并从运行目录可选加载被 Git 忽略的 `server/application-dev.local.yml`。首次使用时，在仓库根目录执行 `cp server/application-dev.local.example.yml server/application-dev.local.yml`，再只在本地文件填写数据库、Redis、MinIO 等连接和账号密码；也可通过环境变量覆盖。禁止将本地文件或真实凭证提交到 Git。H5 使用账号/密码/验证码认证；微信小程序使用后端 code2Session 自动登录。

通知配置的可逆加密需要固定的 `NOTIFICATION_KEY_AES_KEY`（32 字节 Base64 编码），可放在被忽略的本地配置文件对应字段中。部署环境也必须单独安全配置并持续保管该密钥；若丢失，已保存的 Bark/pushplus Key 无法解密。

## 后端启动

启动 MySQL、Redis 和 MinIO，确认数据库 `haji_dev` 可用，然后执行：

```powershell
cd server
mvn spring-boot:run
```

开发启动会通过 Maven 插件设置 `-Dio.netty.noUnsafe=true`，避免 Java 25 输出 Netty 的 `sun.misc.Unsafe` 兼容性警告。直接运行打包后的 JAR 时也建议使用同一 JVM 参数：

```powershell
java -Dio.netty.noUnsafe=true -jar target/hahaen-ledger-server-1.0.0.jar
```

应用主入口也会自动设置该属性，因此 IDEA 直接运行 `LedgerApplication` 时无需额外配置；修改后请完全停止旧进程并重新启动。

`application.yml` 默认激活 `dev` Profile；如需切换环境，设置 `SPRING_PROFILES_ACTIVE`，例如生产环境设置为 `prod`。

后端文件日志目录随 Profile 隔离：`dev` 写入 `D:/github/log/haji`，`prod` 写入 `/home/hahaen/log/haji`。应用进程须对对应目录具备创建和写入权限；控制台日志仍会保留。

应用启动时 Flyway 自动执行 `server/src/main/resources/db/migration/`；当前开发阶段完整数据库初始化基线为唯一的 `V1__init_schema.sql`，不需要手工导入另一份 Schema。DEV 的 MinIO 原始 `9001` 是 Console，SDK/API 使用已验证的 `9000`。Swagger/OpenAPI 地址：`http://127.0.0.1:8080/swagger-ui.html`。

## 前端启动与构建

```powershell
cd app
pnpm install
pnpm run typecheck
pnpm run dev:h5
pnpm run build:h5
pnpm run build:mp-weixin
```

微信小程序构建结果位于 `app/dist/build/mp-weixin`，用微信开发者工具导入即可。H5 构建结果位于 `app/dist/build/h5`。可通过 `VITE_API_BASE_URL` 指向后端地址。

前端环境变量统一放在 `app/env/`：复制 `.env.example` 为 `.env` 管理通用变量，复制 `.env.development.example` 为 `.env.development` 管理 dev 配置，复制 `.env.production.example` 为 `.env.production` 管理 prod 配置并填写实际 HTTPS API 地址。生产构建未配置 `VITE_API_BASE_URL` 会主动失败，避免产物误连接本机开发服务。详见 [app/env/README.md](app/env/README.md)。

## 首版边界

统计、分类、凭证/OCR、预算、周期记账、多人账本、多币种、支付平台同步、导出/删除数据均未做成可用业务入口；帮助页会明确显示暂未开放。

更多约束见 [docs/README.md](docs/README.md)、[AGENTS.md](AGENTS.md)、[docs/09-audit/verification-matrix.md](docs/09-audit/verification-matrix.md) 和 [docs/10-iterations/README.md](docs/10-iterations/README.md)。

### H5 开发页面检查

在 `app` 目录运行 `pnpm run dev:h5`，访问终端打印的 Local 地址（默认 http://localhost:5173）。依赖或 Vite 配置变更后完整重启服务并刷新页面。若端口被占用，Vite 会使用下一个可用端口。H5/微信平台包必须与 uni-app 编译器版本一致，不要用自定义 Vue shim 代替平台运行时。启动验收应直接进入首页，并能切换五项导航；首次使用页仍保留为可选页面。

## 独立物品管理

资产右侧新增“物品”，支持添加、在役/退役筛选、详情、退役出售与逻辑删除。物品按用户独立归属，不影响记账和账户余额。购入当天计第1天，在役日均成本=购入价/服役天数；退役后天数冻结，最终日均成本=(购入价-二手售价)/服役天数。正式迁移新增V6，已有V1–V5保持不变。详见 [物品契约](docs/03-api/items.md) 和 [本轮验收](docs/10-iterations/2026/09/item-lifecycle/09-verification.md)。

## 开发完成后的自动提交

每次修改完成后，按 [AGENTS.md](AGENTS.md) 的“验证、同步与自动提交”规则执行：验证通过 → 拉取当前分支 upstream 并解决冲突 → 复验最终代码 → 自动本地提交，无需再次确认。提交信息使用中文 Conventional Commit，例如 `feat(todo): 新增独立待办清单与周期提醒`。仅提交本次任务内容；推送与发布需另有授权。当次明确要求不提交时，以当次指令为准。
