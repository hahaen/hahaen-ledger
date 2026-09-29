# 数据库

新增 V8 关系表保存重新服役幂等请求，用户与请求键唯一，记录物品 ID；公共字段仅 `created_at`、`deleted`。`personal_item` 现有 CHECK 已允许 ACTIVE 且 `retired_on/resale_cent` 为 NULL，无须修改 V6/V7。自定义 UPDATE 必须显式清空两个字段，不能依赖 MyBatis-Plus `updateById` 的默认 NULL 忽略策略。DEV Flyway V8 已执行；`information_schema` 核对关系表 5 列、中文注释和公共字段可空/默认值通过。
