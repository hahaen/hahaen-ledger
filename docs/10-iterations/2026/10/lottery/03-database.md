# 数据库

新增V12__create_lottery.sql，不修改V1–V11。app_user.lottery_enabled为NOT NULL DEFAULT 0且限定0/1；不更新任何实际账号权限。新增9张完整审计主表：lottery_draw、lottery_draw_revision、lottery_sync_state、lottery_plan、lottery_batch、lottery_entry、lottery_event、lottery_delivery、lottery_attempt；lottery_request为仅带created_at/deleted的幂等关系表。

官方开奖(game,issue)唯一、修订(draw_id,source_hash)唯一；批次位置唯一；事件与渠道唯一。个人表索引按user/plan/batch查询，不增加book_id。全部中文字段注释、utf8mb4_general_ci。用户删除写完整删除审计。

本次只新增带默认值字段和空表，没有旧数据收缩。迁移前仍需核对历史版本、已有字段/表冲突、用户默认权限和结构；生产先完成既有迁移审核与数据库备份/正式Flyway迁移，成功核对后才能更新部署摘要。禁止跳过发布摘要门槛。

实际DEV：PASS，Flyway为V12，默认权限、字段中文注释及全部9张Entity列集合已核对；MySQL/Redis/MinIO探针通过。生产迁移：NOT_RUN，等待必需验收完成，不修改线上迁移摘要。
