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

## Jenkins 自动发布

用户长期授权的执行规则见 [AGENTS.md](../../AGENTS.md#jenkins-自动发布长期授权)。本规则由执行项目任务的代理在交付时遵循，未安装 Git hook 或后台触发器。Review、测试、同步、复审复验、提交及推送通过后，后端改动发布 `haji-server`，任何前端修改均发布 `haji-h5`（包括微信专属改动）；两端同时改先后端，健康检查通过再 H5。纯文档及凭证规则配置不发布。微信小程序上传/提审/发布由用户自行处理，受影响的微信构建验证仍需执行。

### 本地凭证

从根目录 `jenkins.example.json` 复制为 `.local/jenkins.json`，目录权限 700、文件权限 600；`.local/` 整体已加入 `.gitignore`。`base_url` 为 `https://hahaen.xyz/jenkins/`，`folder` 为“哈记账”，`jobs.server` / `jobs.h5` 对应上述任务。在本机编辑器填写 `username` 与 `api_token`，不使用账号密码或任务远程触发 Token 代替用户 API Token；获取入口见 [Jenkins 官方认证说明](https://www.jenkins.io/doc/book/system-administration/authenticating-scripted-clients/)。示例中的凭证始终为空。凭证丢失、空白或权限不足时，报告 BLOCKED，不尝试匿名发布。

禁止输出本地凭证文件、拼接认证命令行、把 Key 放入 URL 或关闭 TLS 校验。经进程内读取构造 Basic 认证头；若使用系统 curl，通过标准输入传配置并使用 `--globoff` 访问带 `tree=...` 的 API 查询，禁止打印配置或响应中的敏感内容。返回的内网/HTTP URL 只取已核验的队列 ID/构建号，用配置的公网 HTTPS 入口重建路径后请求，禁止携带凭证跳转到其他地址。

### 发布与验收

1. 只读查询文件夹及任务 JSON，确认任务、参数、启用状态和部署源。2026-10-15 已通过系统 curl 匿名查询确认文件夹下有两个 WorkflowJob：`haji-server`、`haji-h5`，不代表鉴权或发布验证完成。两个任务当前均为可构建、未排队，并有 `BRANCH` 类型为 `GitParameterDefinition` 的参数；实际发布须用 `buildWithParameters` 并核实所选分支，不依赖上次选值。
2. 当前 `server/Jenkinsfile` / `app/Jenkinsfile` 在远端主机拉取 Gitee `main`，本地 upstream 为 GitHub `origin/main`；必须核实实际 Jenkins 配置和部署源包含目标提交。未同步时报告 BLOCKED，禁止发布旧源码或擅自改远端。注意 Jenkins Pipeline 的 SCM 修订不一定等于 SSH 部署目录的实际修订，需分别核对。
3. 按 [Jenkins Remote API](https://www.jenkins.io/doc/book/using/remote-access-api/) 对无参数任务 POST `build`，参数任务 POST `buildWithParameters`。从响应 Location 提取队列 ID；若触发响应不明先核查，禁止重试造成重复发布。轮询队列对应的 executable，再等待对应构建终态；排队返回 201 不能作为 SUCCESS。不要使用可能属于他人的 `lastBuild` 作为本次结果。
4. 构建 SUCCESS 后核对实际部署提交。后端检查服务健康/启动与必要接口，H5 检查线上 HTML 及资源可访问；如果任务缺少修订或健康证据，明确记录 PARTIAL，不宣称闭环。前后端同时改时，后端发布和健康检查失败/不明必须停止后续 H5 发布。
5. 记录任务、构建号、目标/实际提交、构建结果和线上验收。失败/超时保留已推送代码，按事实报告并查明原因。数据库只按正式 Migration 随后端启动执行，本轮配置规则接入无数据库修改，无需执行 SQL。回滚使用经验证的旧版本，不能假设 Schema 可自动降级。


## 固定分享欢迎图（2026-10-03）

微信分享图与品牌图片共用 `staticResource.ts` 公网根地址，对象为 `haji/wx/share-welcome.png`。公开HTTPS地址应返回HTTP200、image/png；本轮远端文件17580字节、500×400且与备份相同。备份位于 `app/offloaded-static-assets/wx/share-welcome.png`，渲染脚本输出也位于该目录。修改对象内容由用户部署维护，代码地址变更须重新构建和上传小程序。验证与回退见[本轮档案](../10-iterations/2026/10/wechat-share-remote-image/README.md)。
