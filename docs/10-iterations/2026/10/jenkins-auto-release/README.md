# Jenkins 凭证与自动发布规则（历史，已撤销）

> 2026-10-15 用户已撤销本流程及自动发布授权，凭证和模板已删除。本文仅保留既往实施与实测证据，不能作为执行依据；后端、H5 与微信均由用户自行发版，当前规则以根目录 AGENTS.md 为准。

## requirement

用户授权后续任务在 Review、测试、同步、提交及推送通过后自动发布：后端改动发布 `haji-server`，任何前端改动均发布 `haji-h5`；微信小程序不自动上传、提审或发布。真实 Jenkins API Token 只保存在项目本地忽略文件。

## design

以 AGENTS.md 作为执行规则，沿用现有 Jenkins 任务与 Pipeline，不增加 Git hook、后台定时任务或应用业务代码。本地凭证放 `.local/jenkins.json`（目录 700，文件 600）；版本化示例只含空凭证。发布规则核对源码提交、跟踪实际排队项和构建结果，两端同时修改先后端再 H5。

## database / api / backend / frontend

- database：无 Schema、Flyway 或数据修改，无需执行 SQL。
- api：仅调用 Jenkins Remote API，业务 API 不变。
- backend / frontend：沿用 `server/Jenkinsfile` 和 `app/Jenkinsfile`，本轮不修改业务代码、Pipeline 或线上配置。纯凭证及规则接入不触发业务部署。
- 当前 Pipeline 引用 Gitee `main`，本地 upstream 为 GitHub `origin/main`；发布前必须验证目标提交已在实际部署源，不能默认镜像同步完成。

## testing

Review 已仅针对本轮 `.gitignore`、凭证示例、AGENTS.md、README、运维/测试规范、迭代索引及档案差异执行。已核对授权范围、前端必发 H5、先后端后 H5、提交推送前置条件、凭证隔离、真实构建跟踪和 Gitee/GitHub 源码差异；无未解决问题，进入文档与文件检查。不扩大审查到既有业务代码。

本轮必需验证为差异 Review、规则一致性、本地 Markdown 链接、忽略规则、文件权限、无凭证跟踪与 `git diff --check`。业务测试、H5/微信构建、DEV 账务联调不适用。真实鉴权和发布属于凭证配置后的联通验收，与本轮文档及本地文件创建分开记录。

## commands

- 安全 stash 本次 11 个交付文件后执行 `git pull --ff-only`，返回 Already up to date；恢复后逐文件 SHA-256 与同步前完全一致，再删除对应 stash 备份，忽略凭证未参与 stash。同步后仅复审本次差异，无冲突或未解决问题。
- 初始 `git status --short` 无输出；分支 `main`，upstream 为 `origin/main`，HEAD 为 `e6d401a`。
- 已读取 AGENTS.md、README、当前运维/安全/测试规范和两个 Jenkinsfile。
- web 读取线上 Jenkins 未成功；系统 curl（HTTPS 校验开启）访问文件夹只读 API 返回 HTTP 200，确认存在 `haji-server` 和 `haji-h5` 两个 WorkflowJob。
- 两个任务的只读元数据均显示 buildable=true、inQueue=false，参数为 `BRANCH`（GitParameterDefinition）；仅核对元数据，未触发构建。
- Python 内容检查已验证本轮 Markdown 本地链接/锚点、发布规则断言、示例空凭证、JSON 结构、Git 忽略与权限；未输出凭证。`git diff --check` 退出 0。
- Python 标准库 HTTPS 探针遇本机证书链校验失败；未关闭校验，改用系统 curl 成功。首次 curl 未禁用 URL glob，遇方括号解析错误；追加 `--globoff` 后成功。

## verification

PASS：本轮差异 Review、文档链接/锚点、发布规则一致性、空凭证示例及 JSON 结构检查通过；Git 忽略同时覆盖凭证和备份，目录/文件权限为 700/600，凭证未被跟踪；`git diff --check` 退出 0。线上只读文件夹 API HTTP 200，确认两个任务存在。

BLOCKED（后续鉴权验收）：本地用户名和 API Token 尚未填写，未验证身份与 Build 权限。NOT_RUN：真实 Jenkins 构建与线上可用性验收；本轮只接入凭证与规则，不触发业务部署。此限制不影响本轮文档及本地配置文件创建的验证与提交。业务测试、双端构建、DEV/数据库验证不适用；未更改业务实现，无需执行 SQL。

## rollback

回退本轮规则与示例提交可恢复原执行流程。删除本地凭证前应先在 Jenkins 撤销对应 API Token；本轮没有发布，不涉及线上版本回滚。若未来发布需要回滚，选择已验证版本重新构建，数据库变更须单独确认兼容性，禁止自动反向执行 Migration。


## 2026-10-15 追加｜真实凭证与发布验收

### requirement / design

用户已在本地填写凭证，并明确要求测试能否发布。通过进程内读取 `.local/jenkins.json`，认证头经 curl 标准输入传入；不打印 Key、不关闭 HTTPS 校验、不把 Jenkins 返回的 HTTP URL 作为认证请求目的地。仅执行现有两个任务，不改 Jenkins 配置。

### database / api / backend / frontend

- 本地、GitHub `origin/main`、Gitee `main` 均为 `9c58af6dfc987c8a3d9f4fe4cf39c796298459d6`。两个任务实际配置均从 Gitee 加载仓库 Jenkinsfile，参数为 `BRANCH`，本次显式选择 `main`。
- 与上次成功后端 #33 的 SCM 修订 `e683b940` 相比，后端、Compose 和 Migration 无差异；与 H5 #27 的 `1a0220e` 相比，前端无差异。本轮重跑现有版本的部署流程验证凭证，无新增迁移、无需手工执行 SQL。
- 未修改业务代码、API、数据库、Pipeline 或生产配置；微信小程序未上传、提审或发布。

### testing / commands

- 本地凭证文件进程内读取；`whoAmI/api/json` 显示 authenticated=true，身份与配置一致；两个任务元数据/config.xml 均 HTTP 200，可构建，未处于排队/运行状态。
- 发布前以 `git ls-remote` 核对两处远端 `main` 与本地 HEAD 一致；未输出配置内容或凭证。两个任务均 POST `buildWithParameters`，仅传 `BRANCH=main`；响应 HTTP 201 后跟踪各自队列，未重复触发。
- 后端队列 **113** → [haji-server #34](https://hahaen.xyz/jenkins/job/%E5%93%88%E8%AE%B0%E8%B4%A6/job/haji-server/34/)：终态 SUCCESS，SCM 修订为 `9c58af6`。部署日志显示远端工作目录 Fast-forward `1a0220e..9c58af6`、镜像构建完成、haji-server 容器运行。
- 后端发布后，`https://hahaen.xyz/haji-api/api/app/auth/password-key` 返回 HTTP 200、业务 code=0、公钥存在。只读探针不登录、不改真实用户数据；通过后再触发 H5。
- H5 队列 **115** → [haji-h5 #28](https://hahaen.xyz/jenkins/job/%E5%93%88%E8%AE%B0%E8%B4%A6/job/haji-h5/28/)：终态 SUCCESS，SCM 修订为 `9c58af6`，远端源码 Already up to date，日志确认 vue-tsc、编译完成及 Nginx 配置检查通过。
- 发布后 `https://hahaen.xyz/haji/` 返回 HTTP 200 HTML；入口 JS `index-qM33ZaVA.js`（235144 字节）和 CSS `uni.8b028421.css`（12023 字节）、`index-NuBEIWwF.css`（93170 字节）均 HTTP 200，类型正确，非 HTML fallback。
- 本地凭证仍受 Git 忽略，未被跟踪；目录 700、文件 600。临时验证程序在系统临时目录执行，仓库不新增运行脚本或真实凭证。

### verification / rollback

本次证据更新仅 Review 本档案、verification-matrix、third-round-audit 与第三轮档案的新增差异，核对构建号、版本、实际输出、权限隔离、SQL 和未覆盖范围，无未解决问题。文档本地链接/锚点、JSON/忽略规则/权限及 `git diff --check` 检查 PASS；安全 stash 四份证据后执行 `git pull --ff-only` 返回 Already up to date，恢复文件 SHA-256 全部一致后删除备份。同步后复审本次差异无未解决问题，再复验并检查暂存范围。业务实现未改，不重复运行无关业务测试及本地构建。

PASS：真实身份认证、两个任务实际构建触发与 SUCCESS、源码版本核对、后端部署及公开接口探针、H5 编译/部署及入口资源检查。此前因空凭证产生的鉴权 BLOCKED 已解除，真实构建 NOT_RUN 已被本次实测覆盖。本轮无 FAIL/BLOCKED。

PARTIAL：线上验收为公开接口及页面/资源探针，未进行真实账号记账、数据库账务/MinIO 全链路或浏览器视觉验收；不据此宣称全部业务通过。微信工具/真机验证及小程序发布 NOT_RUN，本轮未修改小程序代码。后续文档证据提交不会再次触发业务发布。回滚仍采用前述已验证版本重新构建，不自动降级数据库。
