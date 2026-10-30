# 真实命令

git status --short：工作区及暂存区为空；当前分支main、upstream origin/main。

- `cd app && node --test tests/*.test.mjs`：196/196通过，日志/tmp/haji-help-refresh-tests.log。
- `pnpm run typecheck && pnpm run build:h5 && pnpm run build:mp-weixin`（app目录）：退出0；原生按钮样式修正后类型及双端构建再次通过。
- `node app/tests/wechat-share-build.check.mjs`：25/25通过；Python检查微信帮助WXML/JS，确认无圈出内容和H5刷新入口，无浏览器版本检测代码。
- `python3 /tmp/haji-update-preview.py`：本地5188承载实际生产包及可切换的静态版本响应；Chrome阻止业务API，使用可删除的本地非真实身份标记进入帮助页，不调用/冒充业务接口。
- Chrome实际鼠标点击/Enter、Escape、同版本两次刷新、503/重试及四尺寸通过；新URL保留ui查询与帮助页hash，静态版本3176372f、时间戳两次不同，无重复弹窗；本地标记保留后删除，可见性/尺寸及网络阻止已清理，未操作用户既有页。

同步复审/复验PASS：首次GitHub SSH认证失败，恢复检查曾提前执行；从保留的stash对象恢复，30个任务文件SHA-256逐字节一致。再次安全保存后git pull --ff-only返回Already up to date，apply后重新校验30个文件一致，再删除两份任务备份。仅复审本次差异，无未解决问题；同步后TypeScript、196项回归、实际H5/微信产物及链接、git diff --check复验通过，远端无相关代码变化。

- `git commit -m 'feat(app): 在帮助页新增手动强制刷新入口'`：e5405ec；`git push origin HEAD:main`成功，`git ls-remote origin refs/heads/main`等于本地HEAD e5405ec8ed41483f2eae0c4be6a63aef090a04d3。
- `python3 deploy/local/release.py h5`：退出0，依赖/类型/生产构建与服务器HTTPS验证通过，旧站点移除。
- 系统curl默认TLS校验的公开检查：HTML、7个静态资源（包括帮助页JS及CSS）、haji-version.json逐字节与产物一致；版本c43cd926-c5fa-4c14-826c-18b542fe9350存在于入口JS，未登录账户API401。
- SSH只读核对：h5-release/latest-release为e5405ec，时间2026-10-30T22:29:51+08:00；后端记录及容器OCI revision仍6980b2956a35f594187327d42ae36ae1f58e8650，running/0重启；无haji.incoming.*或ledger-upload.*目录。

上线证据仅文档Review：6份Markdown和生产JSON均与实际结果一致，无未解决问题；本轮链接和git diff --check通过。无源码变化，不重复构建或发布。
