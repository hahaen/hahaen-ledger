# 数据库

新增 V7 `personal_item_edit_request` 幂等关联表，使用 `(user_id, idempotency_key)` 唯一主键，保存 `item_id`、SHA-256 请求摘要、`created_at` 和 `deleted`。用户与物品均设外键；保留记录用于跨后续编辑的安全重试。V1–V6 不修改。迁移执行和 DEV `information_schema` 核验状态：NOT_RUN。
