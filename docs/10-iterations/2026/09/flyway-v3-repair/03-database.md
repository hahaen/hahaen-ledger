# 数据库

- 目标库：DEV 配置指向的 MySQL 数据库；凭证不记录在文档中。
- 迁移：`V3__standardize_audit_columns_and_comments.sql`。
- 原则：不修改已执行历史迁移；所有正式结构变更必须留在 Flyway migration 中。
- 已完成：清理 V3 失败后仅落在空 `app_user` 表中的 8 个临时列，执行 Flyway `repair`，再成功迁移 V3。
- 证据：`flyway_schema_history` 的 V1/V2/V3 均为 `success=true`；V3 checksum 为 `1036275547`；6/6 业务表有表注释，主表审计字段核对为 50/50，6/6 `deleted` 字段为 NOT NULL DEFAULT 0。
- 状态：PASS。
