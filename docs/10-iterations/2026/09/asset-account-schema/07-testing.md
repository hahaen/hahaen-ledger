# 测试

## 本次检查

- 静态检查 Migration 的表名、版本号、公共字段、金额类型、类型检查、金额对应关系、索引和表注释。
- 使用 `git diff --check` 检查新增 SQL 与文档的空白错误。
- 对照 V1/V2 文件和 Git 历史，确认没有修改已存在的历史 Migration。

## 未执行

- 未运行 Flyway/MySQL 实际迁移。
- 未执行 `information_schema`、`flyway_schema_history` 与数据库实体的三方对照。
- 未编写或运行账户业务代码测试，因为本次明确不修改业务代码。
