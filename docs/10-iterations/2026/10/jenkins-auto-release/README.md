# Jenkins 凭证与自动发布规则（2026-10-15）

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
