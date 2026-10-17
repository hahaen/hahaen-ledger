# 2026-10-17｜取消仓库镜像并改为本机直接推送

## requirement

取消 Gitee `ihahaen/hahaen-ledger` 从 GitHub 拉取代码的镜像关系，改用本机 Git 直接推送 Gitee。远端发布仍由用户自行操作。

## design

先检查镜像配置、分支和双方提交，再取消仓库镜像关系并配置本机推送。只做普通快进推送，禁止强制覆盖、删除仓库或改写历史。保留 GitHub 拉取和推送，增加本机到 Gitee 的直接推送；`origin` 两个 pushurl，额外 `gitee` remote 用于检查分支。用户未另选只推 Gitee 时采用双推配置。

## database / api / backend / frontend

无业务、接口、Schema、Flyway、运行配置或前端修改；无需执行 SQL，不触发 Jenkins、H5 或微信发布。

## testing / commands / verification

- 初始工作区和暂存区干净，`main` 跟踪 GitHub `origin/main`，本地 HEAD 为 `d97e218`。
- Gitee 管理页面显示 Pull 镜像：GitHub `hahaen/hahaen-ledger` → Gitee；初始 Gitee main 为 `d3399ed`。
- `GIT_SSH_COMMAND='ssh -o BatchMode=yes -o ConnectTimeout=15' git ls-remote git@gitee.com:ihahaen/hahaen-ledger.git refs/heads/main` 成功返回 Gitee main。
- PASS：Gitee 镜像已取消，管理页显示空列表；对应 GitHub Webhook 已成功更新，Active=0。只停用通知，保留其配置及历史。
- PASS：本机已添加 `gitee` remote 和 `origin` 两个 pushurl，拉取 upstream 仍为 GitHub。
- PASS：`git push origin main` 从本机分别访问两端，GitHub 为 Everything up-to-date，Gitee 快进 `d3399ed..d97e218`；两端 `git ls-remote` 均返回 `d97e218f97477f2cae539bf7d77f928c04744f9a`。
- PASS：本轮文档链接/锚点、本机推送地址及 upstream 断言、`git diff --check` 均通过。最终文档提交及其双端 HEAD 核对由本次交付报告记录。
- 业务测试、双端构建与 DEV 联调不适用：本次只修改仓库推送配置和文档。

## rollback

通过新提交回退当前规范变更；根据本轮记录恢复本机 remote/pushurl。若确需恢复镜像，可重新添加同一 GitHub Pull 镜像；不要触发强制同步覆盖 Gitee 独有提交。回退不恢复已撤销的发布授权。

## Review

已审阅本次 AGENTS.md、README、运维规范、迭代索引、本轮与第三轮档案、两个审计文件，以及本机 Git 配置差异：核对拉取来源、双推目标、非原子失败处理、分支祖先关系、取消镜像与停用对应通知的边界、手动发布和凭证隔离，无未解决问题。业务代码、Pipeline 和其他历史改动不在本轮 Review 范围。

## 实施记录

- `git fetch gitee main` 成功；`git rev-list --left-right --count gitee/main...HEAD` 为 `0 1`，唯一待补提交为已发布的 `d97e218`。
- Gitee WebHooks 页面显示当前仓库没有使用 Webhook；未操作 Jenkins 发布接口。
- 本机配置命令与新克隆步骤见运维规范；修改前 origin 只有 GitHub URL、无 pushurl、无 gitee remote。

- 本轮无 PARTIAL、FAIL 或 BLOCKED。尝试使用 `gh` 检查 Webhook 时本机未安装该命令，已改用登录浏览器成功完成检查和停用，不影响验证。

## 同步与复审

已对 8 个任务文件计算 SHA-256，使用限定文件路径的 `git stash push --include-untracked` 安全保存后执行 `git pull --ff-only`，返回 Already up to date。`git stash apply` 后 8 个文件 SHA-256 完全一致，确认恢复成功才删除本次 stash。同步未引入源码、依赖或冲突；复审本次完整差异及追加证据无未解决问题，文档与配置检查按最终内容再次执行。仅暂存本轮 8 个文档，Git 配置保存在本机；截图证据不随源码提交。
