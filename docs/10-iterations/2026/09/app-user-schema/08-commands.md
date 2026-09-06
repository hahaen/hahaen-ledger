# 命令

## 已执行

- `Get-ChildItem -Recurse -File server/src/main/resources/db/migration`：确认当前迁移目录无可用业务 Migration。
- `git status --short`：确认仓库存在本轮业务域重置的既有删除和修改，本次新增用户 Migration、Flyway 默认配置和迭代记录。
- `git diff --check`：已执行，Migration、Flyway 配置和迭代文档无空白错误。

## 未执行

- 未连接 MySQL 执行 Flyway。
- 未执行后端构建和业务测试，因为本次明确暂不补充业务代码。
