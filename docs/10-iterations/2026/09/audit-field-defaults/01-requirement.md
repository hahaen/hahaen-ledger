# 需求

公共字段中仅 `created_at` 为 NOT NULL，默认当前时间；`deleted` 可空且 DEFAULT 0；其他公共字段允许 NULL。业务主键、金额、归属等必要约束保持原语义。
