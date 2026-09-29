# 实际命令

| 命令 | 结果 |
| --- | --- |
| `JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn -q -Dtest=ItemServiceTest test` | 首次因 Mockito 的 Long 默认值引起新测试错误，补显式空值后复跑 PASS（14 项） |
| `JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn -q -Dtest=ItemDevIntegrationTest -Ditem.dev.verify=true test` | PASS；Flyway V8 在 DEV 执行，真实 HTTP/DB 断言通过 |
| `node --test tests/items.test.mjs tests/date-picker-scroll.test.mjs` | PASS（9 项） |
| `pnpm typecheck` | PASS |
| `pnpm build:h5` | PASS |
| `pnpm build:mp-weixin` | PASS |
| `node --test tests/*.test.mjs` | PASS（87 项） |
| `JAVA_HOME=$(/usr/libexec/java_home -v 25) mvn -q test` | FAIL（68 项：66 通过、1 跳过，`LoggingProfileConfigTest` 1 项 Windows 固定路径断言失败） |
| `git diff --check` | PASS |
