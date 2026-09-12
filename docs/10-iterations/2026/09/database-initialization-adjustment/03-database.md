# database

- 修改 `V1__init_schema.sql`：`avatar_file_id` 改为 `avatar_file_url VARCHAR(512)`。
- 修改 `V2__create_app_file_table.sql`：加入 `idx_app_file_user_avatar_hash`，移除头像外键 ALTER。
- 修改 `V3__create_asset_account_table.sql`：加入 `sort_order` 及 `idx_asset_account_user_type_deleted_order`。
- V1–V4 所有业务表显式使用 `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci`。
- 当前迁移目录按测试阶段基线保留 V1–V4；后续结构变化继续新增版本 Migration，不修改已在其他环境执行的历史迁移。
