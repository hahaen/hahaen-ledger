# 通用通知配置（2026-09-29）

后续 2026-09-30 用户调整：配置已移至独立通知中心，读取接口改为回显 Key；本档案保留首轮历史，当前行为见 [通知中心迭代](../notification-center/README.md)。

## Requirement

为每位用户按通知类型保存可扩展通知 Key；当前个人中心支持 Bark 和 pushplus 两项可选配置，不显示密码小眼睛。数据库使用 utf8mb4 / utf8mb4_general_ci。Key 在客户端沿用密码 RSA-OAEP/SHA-256 传输，服务端可逆加密保存，以便后续发送通知。

## Design

独立通知配置区与个人资料保存互不依赖。已有 Key 只显示“已配置”，输入框留空代表保持原值；显式移除按钮清除配置。接口不返回 Key 或密文。通知类型为可扩展字符串，当前界面列出 BARK 和 PUSHPLUS。

## Database

V9 新增 `user_notification_config` 主表，完整十个审计字段。唯一键 `(user_id, notification_type)` 支持同类型原位更新与软删除后的重新配置。`notification_key` 存 AES-256-GCM 密文，`deleted` 使用 0/1。`user_notification_config_request` 是幂等关系表。V9 已在当前 DEV 环境执行，不再修改该迁移。

## API

`GET /api/app/user/notification-configs` 仅返回已配置类型与布尔状态。`PUT /api/app/user/notification-configs/{type}` 接收 `encryptedKey` 或 `remove=true`，以及必填的 `idempotencyKey`。RSA-2048 OAEP-SHA256 限制通知 Key 至 190 UTF-8 字节；设置与移除互斥。服务端不接受 `userId`，同一幂等键及相同请求可安全重试。

## Backend

`NotificationConfigService` 从 Sa-Token 获取当前用户，写入时锁定有效用户行并在事务中写主表和幂等关系表；查询过滤软删除记录。`NotificationKeyCipher` 使用运行环境中的固定 Base64 32 字节密钥执行 AES-256-GCM，密文含随机 nonce。移除写完整删除审计；重新配置清空删除审计。Key 不进入响应和日志。

## Frontend

个人中心沿用现有白卡片、薄荷绿按钮和输入框样式，资料卡下新增独立通知配置卡。Bark、pushplus 均为选填，不带小眼睛；输入新 Key 替换，留空保留，显式移除后保存。保存按钮有加载/禁用态，失败可重试，成功清空输入并更新配置状态。页面请求仅经 `api.ts`。

## Testing / Commands

2026-09-29 实际执行：`pnpm run typecheck` PASS；`pnpm run build:h5` PASS；`pnpm run build:mp-weixin` PASS；Java 25 `mvn -q -Dtest=NotificationKeyCipherTest,NotificationConfigServiceTest test` PASS（5 项）；Java 25 `mvn -q -Dtest=NotificationKeyCipherTest,NotificationConfigServiceTest,NotificationConfigDevIntegrationTest -Dnotification.dev.verify=true test` PASS（6 项）。首次服务启动时 Flyway 显示从 V8 升至 V9；随后隔离 DEV 集成测试确认 Flyway V9 成功记录、两表结构与字符序、主表 Entity 列集合、RSA 传输、AES 入库、重复请求、未登录拒绝、删除及重新配置。合成账号及相关配置记录已清理，未使用真实通知 Key。Redis 与 MinIO 启动探针也成功。

Java 25 `mvn -q test` FAIL：74 项中 1 项旧 `LoggingProfileConfigTest` 仍断言 Windows 日志目录 `D:/github/log/haji`，当前 macOS DEV 配置实际为 `/Users/hahaen/mi/github`；另 2 项显式 DEV 测试在普通全量运行中跳过。本轮 6 项通知定向/集成测试均单独通过。

## Verification

- PASS：后端定向测试、DEV Flyway 与真实 HTTP/数据库闭环、前端类型检查、H5 和微信构建。
- PARTIAL：通知配置区按现有个人中心样式实现，尚未在真实 H5/微信页面进行视觉与点击验收。
- FAIL：后端普通全量测试的既有日志路径断言失败，与通知配置逻辑无关。
- NOT_RUN：微信开发者工具及真机、真实 Bark/pushplus 消息投递；本轮只交付配置存储，不包含发送功能。

H5 页面检查曾尝试访问现有 `localhost:5180` 的个人中心，但该页面连接的旧 8080 进程返回 500，无法展示资料页；浏览器已恢复到原“我的”页。该尝试不算通知区视觉 PASS，当前开发进程需载入新版后端后再验收。

## Rollback

V9 已在 DEV 执行，不回改迁移。回退应用版本可停用新页面和接口并保留两张新表；保留并备份 `NOTIFICATION_KEY_AES_KEY`，避免未来无法解密既有数据。若需清退数据，应另行制定新迁移并先完成数据保留评估，不执行破坏性 DROP。
