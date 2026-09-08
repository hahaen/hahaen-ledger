# 数据库

- 新增 `V5__add_asset_account_sort_order.sql`。
- `asset_account.sort_order INT NOT NULL`，注释为“同类账户展示顺序，数字越小越靠前”。
- 旧有效账户使用 `ROW_NUMBER() OVER (PARTITION BY user_id, account_type ORDER BY account_name, id)` 初始化。
- 历史 Migration 不修改；目标环境需执行 Flyway 后对照 `flyway_schema_history` 和 `information_schema`。
