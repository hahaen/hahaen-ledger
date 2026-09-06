# 命令

日期：2026-09-06，环境 Windows/PowerShell。下列均实际执行，未记录连接凭证。

1. 读取 AGENTS.md、README、当前规范、产品说明、V1-V3、Entity/Handler 和 API/TypeScript 类型；文件搜索确认六张表及本地 DEV 配置存在。早期数次猜测 Java 路径读取失败，随后通过实际文件搜索定位。
2. `git status --short`：工作区已有修改及大量未跟踪业务目录，本次仅编辑需求相关文件，未暂存/提交。
3. `mvn test`（server）：8 tests，2 failures。匿名测试未显式设置 Mockito Long 返回 NULL；修正测试桩后复验。
4. `mvn test "-Dledger.audit.dev=true"`（server）：9 tests，0 failures/errors/skipped，BUILD SUCCESS；首次执行 V4 成功。
5. `mvn clean package "-Dledger.audit.dev=true"`（server）：9 tests，0 failures/errors/skipped，BUILD SUCCESS；验证 V4 重复启动无待执行迁移。
6. 通过 PowerShell `Start-Process -WindowStyle Hidden` 运行 `java -jar target/hahaen-ledger-server-1.0.0.jar --spring.profiles.active=dev --server.port=0 --logging.level.org.flywaydb=OFF`：检测到应用 Started、Redis PING succeeded、MinIO Bucket available；退出前仅停止本次创建的临时 Java 进程。
7. `git diff --check`：exit 0；仅提示原有 README 的 CRLF/LF 转换。该命令不覆盖未跟踪文件，不据此宣称全部文件已检查。
8. 读取 `server/target/surefire-reports/*.txt`：1 项 DEV 集成、6 项审计、2 项金额测试，均无失败/错误/跳过。

构建有既有 Maven/JDK 原生访问、Unsafe 以及 Mockito 动态 Agent 警告；未导致测试或构建失败。控制台中文测试提示编码异常，JUnit 计数报告正常。
