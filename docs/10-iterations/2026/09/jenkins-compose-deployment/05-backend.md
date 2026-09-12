# 后端

- 新增 `server/Dockerfile`，使用 Maven/Temurin 25 构建并运行 Spring Boot JAR。
- 新增 `server/Jenkinsfile`，通过 SSH 更新部署目录并执行 `docker compose up -d --build haji-server`；仅使用当前 Jenkins 已提供的 Declarative Pipeline 选项，不依赖 Timestamper 插件的 `timestamps()`。
- 后端生产环境变量从 `/home/hahaen/haji-prod.env` 读取，仓库只提供示例文件。
