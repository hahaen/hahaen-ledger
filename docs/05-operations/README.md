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

## 发布方式

后端、H5 和微信小程序均由用户自行发版。代理完成 Review、测试及 Git 提交推送后，不自动触发 Jenkins，也不主动调用 Gitee 镜像同步。原有 Jenkinsfile 与线上任务保持现有配置；Gitee Pull 镜像已取消，对应 GitHub 同步 Webhook 已停用，代码由本机直接推送两端；本项目不再保存本次新增的发布凭证或凭证模板。代理若需操作同步或发布，必须取得当次明确授权。

## 证据要求

每次运行验收记录真实命令、退出码、环境前提、实际输出和未覆盖范围。缺少 MySQL、Redis、MinIO、微信开发者工具或真实凭证时，分别记录 `BLOCKED` 或 `NOT_RUN`；不得使用“端口监听”替代数据库 Schema、事务和对象上传证据。


## 固定分享欢迎图（2026-10-03）

微信分享图与品牌图片共用 `staticResource.ts` 公网根地址，对象为 `haji/wx/share-welcome.png`。公开HTTPS地址应返回HTTP200、image/png；本轮远端文件17580字节、500×400且与备份相同。备份位于 `app/offloaded-static-assets/wx/share-welcome.png`，渲染脚本输出也位于该目录。修改对象内容由用户部署维护，代码地址变更须重新构建和上传小程序。验证与回退见[本轮档案](../10-iterations/2026/10/wechat-share-remote-image/README.md)。


## 本机直接双推

2026-10-17 起，本机直接推送 GitHub 与 Gitee。`main` 继续跟踪 GitHub `origin/main`；`origin` 设置两个 pushurl，`gitee` 用于独立 fetch/核对。Gitee 不再从 GitHub 拉取镜像，对应 GitHub Webhook 已停用；仓库导入产生的手动强制同步入口可能仍显示，不再使用。

以下配置保存在本机 `.git/config`，不会随源码克隆。新克隆、确认 `origin` 尚无自定义 pushurl 且没有 `gitee` remote 后执行一次；已有配置先用 `git remote -v` 检查，避免重复追加：

```sh
git remote add gitee git@gitee.com:ihahaen/hahaen-ledger.git
git remote set-url --add --push origin ssh://git@ssh.github.com:443/hahaen/hahaen-ledger.git
git remote set-url --add --push origin git@gitee.com:ihahaen/hahaen-ledger.git
```

完成项目规定的 Review、验证和 upstream 拉取后，先 `git fetch gitee main` 并审查 `git log --oneline HEAD..gitee/main` 与 `git log --oneline gitee/main..HEAD`；若有远端独有提交，先完成语义同步及复验，再执行 `git push origin main`。两个 pushurl 的推送分别执行，不能保证跨平台原子成功；发生一端失败时如实报告，处理后普通推送失败的一端，不强推。

推送后分别执行 `git ls-remote ssh://git@ssh.github.com:443/hahaen/hahaen-ledger.git refs/heads/main`、`git ls-remote git@gitee.com:ihahaen/hahaen-ledger.git refs/heads/main`，与 `git rev-parse HEAD` 对照。双端一致才算交付完成。此流程只传递源码，不触发代理发布。
