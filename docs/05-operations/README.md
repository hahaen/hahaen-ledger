# 05｜运行与运维

本目录说明本地开发、构建、启动、配置、日志和故障排查方式。它关注“如何安全地运行项目”，不替代具体功能的验证记录。

## 实际环境入口

- 后端：Java 25、Maven 3.9+（当前环境 Maven 3.6.3 可执行）、Spring Boot 3.5.5；依赖 MySQL 8、Redis 和 MinIO。
- 前端：Node.js、pnpm；脚本定义在 `app/package.json`，包括 `dev:h5`、`build:h5`、`dev:mp-weixin`、`build:mp-weixin` 和 `typecheck`。
- 后端默认激活 `dev` Profile，端口默认 8080；开发配置当前位于 `server/src/main/resources/application-dev.yml`，仓库没有同名 `application-dev.example.yml`，不要根据旧 README 复制不存在的文件。
- 前端环境文件位于 `app/env/`。当前工作区实际保留 `.env`/`.env.example` 及本机忽略的 mode 文件；使用前应以该目录实际文件为准，不能假设每个 mode 都有 `.example` 文件。

## 数据库与依赖

Flyway 由应用启动执行，位置为 `classpath:db/migration`，当前仓库包含 V1–V4。开发 Profile 明确关闭 `baseline-on-migrate`；`application.yml` 的默认值不能替代真实环境的迁移历史检查。Redis 临时 Key 必须有 TTL，Sa-Token 会话和验证码 Key 使用 `haji:` 命名空间；禁止 `FLUSHALL`/`FLUSHDB`。MinIO 只能通过 `MinioStorageService` 访问，业务代码不保存永久 URL。

生产环境必须通过环境变量提供数据库、Redis、MinIO、CORS、微信和 H5 RSA 私钥等配置；生产配置中的 `GENERATE` 仅是兜底值，不能作为生产密钥策略。真实凭证只放本地忽略文件或部署密钥系统，不能进入文档、命令和日志。

生产后端文件日志配置为 `/home/hahaen/log/haji`。Docker Compose 将该宿主机目录 bind mount 到容器内同一路径；通过 Jenkins 使用仓库中的 `docker-compose.prod.yml` 部署后，可直接在宿主机查看该目录。首次启用挂载前，先备份需要保留的旧容器内日志，因为挂载会遮住容器原目录内容。应用控制台日志也可通过 `docker logs haji-server` 查看。

MinIO 的 `MINIO_ENDPOINT` 是应用服务器访问 MinIO 的内网 API 地址。若该地址不能被浏览器/微信客户端访问，生产环境还须在部署环境配置 `MINIO_PUBLIC_URL_PREFIX`。后端仍以 `MINIO_ENDPOINT` 生成签名，仅替换返回 URL 的外部前缀；Nginx 必须将公网前缀去前缀代理至同一内网 Endpoint，并让上游收到与签名一致的 Host（使用该 Endpoint 作为 `proxy_pass` 时保留 Nginx 默认 `$proxy_host`）。生产 Profile 要求提供非空 `MINIO_PUBLIC_URL_PREFIX`，缺少时应用启动失败；不要把 MinIO Console 地址 `9001` 配成对象 API 地址。

## 常用命令

```powershell
cd server
mvn test
mvn spring-boot:run

cd ..\app
pnpm install
pnpm run typecheck
pnpm run dev:h5
pnpm run build:h5
pnpm run build:mp-weixin
```

生产 H5 构建未配置 `VITE_API_BASE_URL` 时应失败；构建通过只证明编译和打包，不证明 MySQL、Redis、MinIO、真实登录或微信工具联调通过。

## 证据要求

每次运行验收记录真实命令、退出码、环境前提、实际输出和未覆盖范围。缺少 MySQL、Redis、MinIO、微信开发者工具或真实凭证时，分别记录 `BLOCKED` 或 `NOT_RUN`；不得使用“端口监听”替代数据库 Schema、事务和对象上传证据。
