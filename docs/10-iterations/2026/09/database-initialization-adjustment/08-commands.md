# commands

| 工作目录 | 命令 | 结果 |
| --- | --- | --- |
| `server` | `mvn -Dtest=MigrationIntegrityTest test` | PASS，1/1；`BUILD SUCCESS` |
| 仓库根目录 | `git diff --check` | PASS，无空白错误 |
| MySQL 容器 | 只读查询 `information_schema.SCHEMATA` | PASS（已执行）；当前未发现 `haji_dev`，因此重建后的结构查询仍为 NOT_RUN |
