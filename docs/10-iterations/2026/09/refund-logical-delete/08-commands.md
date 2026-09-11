# commands

- `server`: `mvn test`：PASS，26 tests，0 failures/errors/skipped；包含删除最后一笔退款后 `hasRefund=0` 的断言。
- 开发 MySQL 容器：对目标退款及所属账单执行只读 `SELECT`：PASS，确认退款行 `deleted=0`、`deleted_at` 已设置；未输出凭据。
- `git diff --check`：PASS；仅有既存工作区文件的 CRLF 提示，未报告空白错误。
- 当前 8080 Java 进程在本次编译之前启动：NOT_RUN，未擅自重启，也未重试写接口。
