# 后端

`AuditMetaObjectHandler` 保留 createdAt 和 deleted=0 插入填充。有真实登录身份时填充创建人，未知时不再用记录 ID 或 SYSTEM_USER_ID 兜底。显式创建人/名称快照、创建时间及删除状态均保留；系统任务仍可显式使用集中身份常量。

`BaseAuditEntity` 和 `BaseRelationEntity` 注释与 V4 同步，LocalDateTime/Long/Integer 等引用类型已支持空值，无需类型替换。新增六项处理器单元测试，覆盖匿名含 ID/无 ID、登录身份、显式审计快照、更新和关联表。DEV 启动、Redis PING、MinIO Bucket 探测均 PASS。
