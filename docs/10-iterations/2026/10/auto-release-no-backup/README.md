# 修改完成后自动发布，不备份旧版本

## requirement

2026-10-25 用户授权后续代码修改完成、Review、验证、同步、提交及推送通过后，自动在服务器发布；不备份之前版本。不恢复 Jenkins，不增加 Git hook 或后台监听。用户追加要求：最后明确是否发布成功、哪些组件成功；部分成功分项报告。

## design

由完成修改的代理按 AGENTS.md 自动调用本机 release.py：后端变化发布 backend，H5 变化发布 h5，两者变化发布 all（先后端再 H5）。部署工具变化发布 all 验证新流程。纯文档或仅微信专用修改无需服务发布。任意手工 Git push 不会独立触发发布。

Ledger 不复制旧 release.env、Compose 或网页，不自动回滚旧容器；新配置在替换容器前写入稳定目录，失败保留实际目标并报错，不写成功标记。后端/H5 各自通过 HTTPS 检查后写分组件版本记录并打印成功；后端成功后立即清理旧镜像，再开始 H5。全部完成才写整体成功记录。数据库、业务文件、日志、运行密钥保留，Migration 摘要门槛不变。

## database

无 Schema/Migration/业务 SQL 变化；取消应用版本备份不授权删除数据库或数据备份。

## api

业务 API 不变；保留未登录 API 401 和 H5 入口 HTTPS 探针。

## backend

应用业务代码不变；仅修改 Ledger 部署流程。共享脚本的 Radio 分支保持原行为。

## frontend

H5 直接替换产物；微信小程序继续由用户独立上传、提审和发布。

## testing

计划验证无旧版本复制、发布成功、失败不回滚、迁移摘要变化阻断，以及既有产物安全回归、Bash 语法、文档链接。正式运行验证将由 release.py all 执行 Java 25 Maven verify、H5 类型检查/生产构建与服务器探针。

## commands

- 首轮 Review：仅本次部署脚本、6 项新增流程回归和规则/文档；修正 H5 新产物先准备再替换、分组件记录、线上 HTML 内容及容器 revision 核对、Radio 分支保留原行为。复审无未解决问题。
- `python3 -B -m unittest discover -s deploy/local -p 'test_*.py' -v`：退出 0，10/10 PASS（6 项部署流程、4 项既有产物安全）；覆盖部分成功分项报告、失败不恢复、旧页面误报及迁移门槛。
- `bash -n deploy/local/deploy.sh`、`git diff --check`：退出 0。
- Java 25 `mvn -B -f server/pom.xml clean verify`：退出 0，BUILD SUCCESS；96 项中 90 项执行通过、6 项专用 DEV 环境测试按默认开关跳过；无业务/数据库改动，不将跳过项宣称通过。
- `app/` 内 `pnpm run typecheck && pnpm run build:h5`：退出 0；已有 Sass legacy API 弃用提示，无构建失败。
- 修改文档 220 条本地链接存在性检查通过。
- SSH 只读前置核对：后端容器 running，迁移摘要文件存在；未输出运行密钥。

## verification

PASS：首轮 Review、10 项部署/安全回归、Java 25 验证构建、H5 类型检查/构建、Bash 语法和文档检查。PASS：本轮 stash `fc0e8a0cd067f9ce028a7bb09c219ae94b100af2` 保存；`git pull --ff-only` 返回 Already up to date，恢复后 11 文件 SHA-256 与暂存状态完整核对才删除备份，无冲突；同步后差异复审及 10 项回归、Bash 语法、Java 25 verify（90 执行通过/6 默认跳过）、H5 类型检查/构建再次通过。NOT_RUN：正式发布及线上版本核对（提交推送后执行）。微信工具/客户端验证不适用，本轮不修改客户端业务。

## rollback

无旧版本备份或自动回滚。需要恢复时从 Git 选择兼容提交重新编译上传，不自动降级数据库。
