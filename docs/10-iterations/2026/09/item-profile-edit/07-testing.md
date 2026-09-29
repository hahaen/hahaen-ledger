# 测试

- 后端单测及API/数据库集成测试：NOT_RUN（本轮未新增或运行测试）。
- 前端类型检查：PASS，`pnpm run typecheck`。
- H5、微信小程序生产构建：PASS，`pnpm run build:h5`、`pnpm run build:mp-weixin`。
- 后端打包：PASS，Java 25 下 `mvn -q -f server/pom.xml -DskipTests package`；未运行测试。
- Git 空白检查：PASS，`git diff --check`。
- DEV Flyway 与 `information_schema` 核验：NOT_RUN。
- 微信开发者工具及真机验收：NOT_RUN。
