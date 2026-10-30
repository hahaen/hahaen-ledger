# 05｜运行与运维

本目录说明本地开发、构建、启动、配置、日志和故障排查方式。它关注“如何安全地运行项目”，不替代具体功能的验证记录。

## 实际环境入口

- 后端：Java 25、Maven 3.9+（当前环境 Maven 3.6.3 可执行）、Spring Boot 3.5.5；依赖 MySQL 8、Redis 和 MinIO。
- 前端：Node.js、pnpm；脚本定义在 `app/package.json`，包括 `dev:h5`、`build:h5`、`dev:mp-weixin`、`build:mp-weixin` 和 `typecheck`。
- 后端默认激活 `dev` Profile，端口默认 8080；`server/src/main/resources/application-dev.yml` 只保留安全默认值和环境变量引用。本机连接、账号和密码写入被 Git 忽略的 `server/application-dev.local.yml`；首次使用从 `server/application-dev.local.example.yml` 复制。
- 前端环境文件位于 `app/env/`。当前工作区实际保留 `.env`/`.env.example` 及本机忽略的 mode 文件；使用前应以该目录实际文件为准，不能假设每个 mode 都有 `.example` 文件。

## 数据库与依赖

Flyway 由应用启动执行，位置为 `classpath:db/migration`，当前仓库包含 V1–V4。开发 Profile 明确关闭 `baseline-on-migrate`；`application.yml` 的默认值不能替代真实环境的迁移历史检查。Redis 临时 Key 必须有 TTL，Sa-Token 会话和验证码 Key 使用 `haji:` 命名空间；禁止 `FLUSHALL`/`FLUSHDB`。MinIO 只能通过 `MinioStorageService` 访问，业务代码不保存永久 URL。

dev 环境可在 `server/application-dev.local.yml` 配置数据库 URL/账号/密码、Redis、MinIO、CORS、微信和 H5 RSA 私钥；该文件受 `.gitignore` 保护，提交前仍需检查 Git 状态。生产环境必须通过环境变量提供同类配置；生产配置中的 `GENERATE` 仅是兜底值，不能作为生产密钥策略。真实凭证不能进入被跟踪文件、文档、命令和日志。

生产后端文件日志配置为 `/home/hahaen/log/haji`。Docker Compose 将该宿主机目录 bind mount 到容器内同一路径；通过本机上传入口使用运行 Compose 部署后，可直接在宿主机查看该目录。首次启用挂载前，先备份需要保留的旧容器内日志，因为挂载会遮住容器原目录内容。应用控制台日志也可通过 `docker logs haji-server` 查看。

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

## 发布方式

代理完成代码修改、Review、必需验证、upstream 同步、复审复验、提交推送并核对远端后，自动执行受影响的 `backend` 或 `h5` 发布；两端或部署工具变化执行 `all`，先后端后 H5。纯文档/仅微信专用变更不发布。长期授权和范围见 [AGENTS.md](../../AGENTS.md)。此流程由代理在任务内执行，不安装 Jenkins、Git hook、GitHub Actions 或后台监听，单独手工 push 不会触发发布。

从仓库根执行 `python3 deploy/local/release.py all`，可选 `backend`、`h5`。本机需 Java 25、Maven、Node/pnpm、生产 H5 环境配置，以及可严格验证主机密钥的 SSH 别名 `hahaen-server`。`--host` 可选择其他已配置目标，`--build-only` 只验证编译，不连接服务器。必须先提交跟踪文件改动，产物绑定提交号。

后端先 `mvn clean verify`，H5 先类型检查/生产构建；仅上传 JAR/H5、运行 Dockerfile、Compose 与发布脚本。服务器不安装 Maven、pnpm 或应用源码。Docker 在 `linux/amd64` 下仅复制产物打包镜像，不编译源码。本机不需要安装 Docker。

服务器运行配置在 `/home/hahaen/ledger-runtime/{compose.yaml,release.env}`，其中 `HAJI_IMAGE` 指向当前不可变标签；私有配置仍为 `/home/hahaen/haji-prod.env`，JAR 不进入 Git。Nginx 与后端共用外部网络 `haen_application`；H5 目录为 `/home/hahaen/nginx/html/haji`。发布加排他锁，后端以未登录 API 的 HTTP 401 检查真实 HTTP 服务，新运行配置在替换容器前写入稳定目录，经过 Nginx 验证后记录成功并删除旧镜像/临时文件；不复制旧配置或网页，不创建旧应用版本备份，不自动恢复旧容器或网页，失败返回非零并保留实际目标状态供修复重发；迁移材料变化时拒绝普通更新，必须先完成审核、备份与正式迁移，再记录实际摘要；数据库保持 Flyway 正向规则，不反向迁移。

成功后只留当前版本；需要回退时从 Git 选择兼容版本重新编译。保留生产 MySQL/Redis/MinIO、密钥、证书、业务文件和日志，不执行全局镜像或卷清理。用户已另行长期授权代理按上述门槛自动发布后端/H5；微信小程序不经此入口发布。

## 证据要求

每次运行验收记录真实命令、退出码、环境前提、实际输出和未覆盖范围。必需验证缺少 MySQL、Redis、MinIO 或真实凭证时，分别记录 `BLOCKED` 或 `NOT_RUN`；不得使用“端口监听”替代数据库 Schema、事务和对象上传证据。

2026-10-30 起，现阶段以 H5 为主要开发、运行验收与交付目标，微信小程序仍同步开发、检查和按影响范围命令行构建。代理不启动或操作用户本机微信开发者工具，不通过 UI、CLI 或自动化进行小程序导入、运行测试、调试、预览、上传、提审或发布。微信工具/真机验收与发布按阶段规则排除，记录为非必需 `NOT_RUN`，不阻塞 H5 交付；后端/H5 自动发布流程继续执行。详见 [平台操作边界](../../AGENTS.md#当前阶段的平台操作边界2026-10-30)。


## 固定分享欢迎图（2026-10-03）

微信分享图与品牌图片共用 `staticResource.ts` 公网根地址，对象为 `haji/wx/share-welcome.png`。公开HTTPS地址应返回HTTP200、image/png；本轮远端文件17580字节、500×400且与备份相同。备份位于 `app/offloaded-static-assets/wx/share-welcome.png`，渲染脚本输出也位于该目录。修改对象内容由用户部署维护，代码地址变更须重新构建和上传小程序。验证与回退见[本轮档案](../10-iterations/2026/10/wechat-share-remote-image/README.md)。


## GitHub 单端推送

`main` 跟踪 GitHub `origin/main`，`origin` 只有 GitHub 推送地址。已移除 Gitee remote 与双推；不再新增镜像同步或 Webhook。按全局顺序安全保存、拉取 upstream、恢复、复审复验、逐文件暂存，提交后普通推送并核对远端 HEAD。


## 独立彩票（V12）

见[彩票功能规范](lottery.md)，默认无权限，独立于账本，官方结果本地持久化。
