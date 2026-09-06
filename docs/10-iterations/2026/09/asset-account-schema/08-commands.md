# 命令

以下为本轮实际执行的只读检查或静态校验命令；输出不包含凭证。

- `Get-ChildItem -Force`、`rg --files ...`：确认项目结构、数据库迁移目录和规范文档。
- `Get-Content`：读取 README、数据库规范、V1/V2 Migration、原型账户定义和审计文档。
- `git status --short`、`git log --oneline --all -- server/src/main/resources/db/migration`：确认工作区初始状态及历史迁移记录。
- `Test-NetConnection -ComputerName 127.0.0.1 -Port 3306`：确认本机端口可连，但本轮未使用数据库客户端执行迁移。
- `git diff --check`：检查变更文件空白错误，结果为通过。

Flyway/MySQL 执行：NOT_RUN。
