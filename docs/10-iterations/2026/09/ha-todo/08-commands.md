# 实际执行命令与结果

- `JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn -q -DskipTests compile`：PASS。
- `JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn -q -Dtest=TodoScheduleTest,TodoReminderWorkerTest test`：PASS。
- `JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn -q -Dtest=TodoDevIntegrationTest -Dtodo.dev.verify=true test`：首次 FAIL，发现自动写入审计填充读取缺失 Sa-Token 上下文；修复后重跑 PASS，Flyway V10 执行并核对 Schema。
- `JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn -q -Dtest=TodoApiDevIntegrationTest -Dtodo.dev.verify=true test`：PASS；新增提醒记录读取断言后复跑 PASS。
- `pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin`：PASS。
- `JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn -q test`：FAIL，既有 `LoggingProfileConfigTest` DEV 日志目录断言与当前配置不一致。
- `JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn -q -Dtest=TodoScheduleTest,TodoReminderWorkerTest,TodoDevIntegrationTest,TodoApiDevIntegrationTest -Dtodo.dev.verify=true test`：最后一次后端修复后 PASS，包含编辑规则不重现已完成日期且保留原始锚点的真实 HTTP 断言。
- 最后一次前端弹窗修复后复跑 `pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin` 和 `git diff --check`：均 PASS。

首次直接 `mvn` 使用本机默认 JDK 时不支持 Java 25；后续均指定 Java 25。命令及本档案不包含真实凭证。

2026-09-30 独立表单页补测：`JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn -q -Dtest=TodoScheduleTest,TodoReminderWorkerTest,TodoDevIntegrationTest,TodoApiDevIntegrationTest -Dtodo.dev.verify=true test` PASS；最后一次修改后 `pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin` 均 PASS。微信开发者工具使用 `pnpm run dev:mp-weixin` 重新生成开发目录，原监听进程仅产出编辑页 JSON 而缺 WXML，重启编译后模拟器恢复。
