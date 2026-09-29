# 通知配置表

V9 `user_notification_config` 以 `id` 为主键，`(user_id, notification_type)` 唯一。通知类型为可扩展的大写 ASCII 字符串；当前界面提供 `BARK`、`PUSHPLUS`。`notification_key` 是 `v1:` 前缀的 AES-256-GCM 密文，包含每次随机生成的 12 字节 nonce；后端发送服务可通过 `NotificationKeyCipher` 解密。数据库和日志不保存明文；当前用户的读取接口解密后返回 Key，用于通知中心回显，响应禁止缓存。主表继承完整十个公共审计字段；移除配置写删除审计，重新配置清除删除审计并沿用原记录。

`user_notification_config_request` 是关系表，以 `(user_id, idempotency_key)` 为主键，存请求摘要、`created_at`、`deleted`。相同请求键与相同摘要可重复提交；同键不同内容拒绝。两表使用 `utf8mb4` / `utf8mb4_general_ci`，类型及幂等键列另用 `ascii_bin` 保持精确比较。

加密密钥由运行环境 `NOTIFICATION_KEY_AES_KEY` 提供，值为固定 32 字节的 Base64 编码；丢失或更换旧密钥会使既有密文无法解密。DEV 本地文件已设置且被 Git 忽略；其他环境须分别安全配置。V9 已在当前 DEV 执行，不得回改。
