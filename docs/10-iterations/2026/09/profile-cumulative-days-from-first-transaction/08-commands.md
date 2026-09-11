# 实际执行命令

| 命令 | 结果 |
| --- | --- |
| `server: mvn -Dtest=ProfileServiceTest test` | PASS：3 tests，0 failures/errors/skipped，BUILD SUCCESS。 |
| `server: mvn test` | PASS：29 tests，0 failures/errors/skipped，BUILD SUCCESS。 |
| `git diff --check` | PASS：退出码 0；仅输出既有工作区文件的 CRLF/LF 提示。 |

本文件只记录本次实际运行的命令。
