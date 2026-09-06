# 05｜运行与运维

本目录说明本地开发、构建、启动、配置、日志和故障排查方式。它关注“如何安全地运行项目”，不替代具体功能的验证记录。

## 环境边界

后端依赖 Java 25、Spring Boot 3.5、MySQL 8、Redis 7.4 和 MinIO；前端依赖 Node.js 22、pnpm 11。真实开发配置放在被 Git 忽略的 `application-dev.yml` 和前端环境文件中，仓库只保留 example。

## 运行原则

Flyway 由应用启动时执行，Redis 临时 Key 必须有 TTL，MinIO 只能通过 `MinioStorageService` 访问。日志要有 Trace ID、分级和脱敏，命令、文档和输出中不得出现密码、Token、AppSecret、Access Key 或永久签名 URL。

## 证据要求

每次运行验收记录真实命令、环境前提、结果和限制。缺少 MySQL、Redis、MinIO、微信开发者工具或真实凭证时，应记录 `BLOCKED`，不能用编译通过替代联调证据。
