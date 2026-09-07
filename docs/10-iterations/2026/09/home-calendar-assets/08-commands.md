# 08｜命令

本文件只记录真实执行命令。开发过程中逐项追加命令、退出码和关键结果，不记录任何真实凭证、Token、签名 URL 或完整环境变量。

| 命令 | 结果 |
| --- | --- |
| `mvn test -DskipTests`（`server`） | PASS，后端 73 个主类编译通过。 |
| `mvn test`（`server`） | PASS，18 tests，0 failures/errors/skipped。 |
| `pnpm run typecheck`（`app`） | PASS，`vue-tsc --noEmit` exit 0。 |
| `pnpm run build:h5`（`app`，未注入地址） | FAIL，按项目规则拒绝缺少生产 API 地址。 |
| `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5` | PASS，H5 构建完成。 |
| `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin` | PASS，小程序构建完成。 |
| `mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=18080 --spring.flyway.enabled=false"` | PASS，独立端口启动；Redis PING、MinIO bucket 探针成功；未执行 Flyway/DDL。 |
| `GET http://127.0.0.1:18080/api/app/accounts` | PASS，未登录返回 HTTP 401。 |
| `GET http://127.0.0.1:18080/api-docs` 并检查路径 | PASS，首页/日历/资产/账户/账单 12 个路径出现。 |
| `Test-NetConnection 127.0.0.1 -Port 3306/6379/9000` | PASS，三个端口监听；MySQL 真实 schema/事务仍未查询。 |
| `git diff --check`、Migration diff 检查 | PASS，无空白错误，Migration 无差异。 |
