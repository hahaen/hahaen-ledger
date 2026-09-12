# verification

| 范围 | 状态 | 证据 |
| --- | --- | --- |
| V1–V4 迁移静态结构 | PASS | 当前测试阶段所需字段、索引和 utf8mb4_general_ci 已写入初始化脚本 |
| 当前 Flyway 初始化文件 | PASS（静态） | 迁移目录当前包含 V1–V4，后续结构变化仍可新增版本 |
| 迁移完整性测试 | PASS | `MigrationIntegrityTest` 1/1 |
| haji_dev 重建后表结构 | NOT_RUN | 当前 MySQL 未发现 `haji_dev`，尚未重新建库建表 |
| 真实应用启动和 Flyway 执行 | NOT_RUN | 等待数据库重新创建后验证 |
