# 当前验证矩阵

更新日期：2026-09-08。状态只表示当前工作区实际证据：`PASS`=已执行且符合预期；`PARTIAL`=部分完成；`FAIL`=已执行但不符合预期；`BLOCKED`=外部条件不可得；`NOT_RUN`=尚未执行。

## 当前实现与静态核对

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
| 微信小程序认证 | `app/src/stores/ledger.ts` 调用 `/api/app/auth/login`；当前后端无该路径 | FAIL（接口不匹配） |
| H5 头像链路 | `FileController`、`AppFileService`、`MinioStorageService`、`utils/file.ts` | PARTIAL（代码存在；真实对象上传未在本次执行） |
| 账单附件 | Schema 预留 `TRANSACTION_ATTACHMENT`，当前业务页面/Service 未开放 | NOT_RUN |

## 自动化与运行证据

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 后端测试 | `server`: `mvn test`；18 tests，0 failures/errors/skipped，BUILD SUCCESS | PASS |
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

## 当前高风险项

1. 在补齐并验证 `/api/app/auth/login` 前，微信小程序不能作为认证已闭环发布。
2. 账户名称只有 Service 级重复校验，V3 没有数据库唯一约束；并发创建同名账户仍需产品/数据库决策。
3. Migration 文件存在不代表目标环境已经执行；必须在可用 MySQL 上补做 Flyway、`information_schema`、事务和并发验证。
4. `application-prod.yml` 对 H5 RSA 私钥使用生成兜底，部署必须显式注入固定私钥；安全检查不能只依赖默认配置。
