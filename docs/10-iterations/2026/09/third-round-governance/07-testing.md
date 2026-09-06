# 测试

本轮要求执行 `server/mvn test`、`server/mvn clean package`、`app/pnpm install`、`app/pnpm run typecheck`、`app/pnpm run build:mp-weixin`、`app/pnpm run build:h5`，并在可用 DEV 环境做 Flyway、MySQL、Redis、MinIO 和核心 HTTP 回归。当前记录状态由 `09-verification.md` 维护。
