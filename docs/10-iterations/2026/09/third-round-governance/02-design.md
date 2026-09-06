# 设计

采用新增 V3 迁移而不修改 V1/V2；主表继承 `BaseAuditEntity`，关联表继承 `BaseRelationEntity`；由 MyBatis-Plus `MetaObjectHandler` 统一 Insert/Update 填充，用户主动删除由 Service 显式写删除审计。docs 按当前规范、阶段审计、历史迭代分层。
