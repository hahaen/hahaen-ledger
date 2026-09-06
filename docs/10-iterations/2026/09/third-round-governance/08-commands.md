# 命令证据

已实际执行并通过：`server/mvn test`（2 tests，0 failures/errors）、`server/mvn clean package`、`app/pnpm install --frozen-lockfile`、`app/pnpm run typecheck`、`app/pnpm run build:h5`、`app/pnpm run build:mp-weixin`。已实际尝试 `server/mvn spring-boot:run '-Dspring-boot.run.profiles=dev'`，应用进入 dev profile 后因本机 MySQL 连接拒绝退出；不记录任何凭证。第一次未加引号的 Maven 命令因 PowerShell 参数解析失败，不作为启动证据。
