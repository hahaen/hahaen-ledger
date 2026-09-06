# 数据库

新增 `V4__relax_audit_fields_and_set_defaults.sql`，保留 V1-V3。六张业务表的 `created_at` 为 DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)，`updated_at` 可空，`deleted` 为 TINYINT NULL DEFAULT 0；五张主表的 `created_by` 改为可空，其余公共列原本已可空。

真实 `haji_dev` 已执行 V4，Flyway migrate/validate 成功。information_schema 共核对 53 个公共/历史审计列（主表 50 + 关联表 3），默认值、可空及中文注释全部通过。V4 不回写历史业务数据。

六表省略公共字段的插入均成功：created_at 自动生成，created_by/updated_at 为 NULL，deleted 为 0。六表显式 deleted=NULL 均成功，created_at=NULL 被数据库拒绝；临时行已事务回滚并逐表确认不存在。关联表历史 updated_at 保留可空，BaseRelationEntity 仍只映射 created_at/deleted。
