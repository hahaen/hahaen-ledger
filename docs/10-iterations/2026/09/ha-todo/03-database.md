# 数据库

V10 是正式迁移，新增五表，均 `CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci`：规则、发生项、渠道投递、每次发送尝试、幂等请求。业务四表十字段审计，请求关系表只有 `created_at/deleted`。发生项保存规则快照，尝试表逐次保存渠道、实际标题/正文、结果和尝试时间，不存通知 Key。详见 [当前数据库规范](../../../../02-database/ha-todo.md)。

2026-09-30 DEV 启动时从 V9 升到 V10，后续再启动 Flyway 校验 10 个迁移成功。`TodoDevIntegrationTest` 查询 `information_schema` 验证五表字符序和所有列中文注释，并核对 `TodoRule`/`TodoOccurrence` 加 `BaseAuditEntity` 字段的列集合，插入合成用户与发生项并清理。V10 已执行，不得回改。
