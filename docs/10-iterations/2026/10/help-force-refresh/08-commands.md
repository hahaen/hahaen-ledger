# 真实命令

git status --short：工作区及暂存区为空；当前分支main、upstream origin/main。

- `cd app && node --test tests/*.test.mjs`：196/196通过，日志/tmp/haji-help-refresh-tests.log。
- `pnpm run typecheck && pnpm run build:h5 && pnpm run build:mp-weixin`（app目录）：退出0；原生按钮样式修正后类型及双端构建再次通过。
- `node app/tests/wechat-share-build.check.mjs`：25/25通过；Python检查微信帮助WXML/JS，确认无圈出内容和H5刷新入口，无浏览器版本检测代码。
- `python3 /tmp/haji-update-preview.py`：本地5188承载实际生产包及可切换的静态版本响应；Chrome阻止业务API，使用可删除的本地非真实身份标记进入帮助页，不调用/冒充业务接口。
- Chrome实际鼠标点击/Enter、Escape、同版本两次刷新、503/重试及四尺寸通过；新URL保留ui查询与帮助页hash，静态版本3176372f、时间戳两次不同，无重复弹窗；本地标记保留后删除，可见性/尺寸及网络阻止已清理，未操作用户既有页。

同步复审/复验PASS：首次GitHub SSH认证失败，恢复检查曾提前执行；从保留的stash对象恢复，30个任务文件SHA-256逐字节一致。再次安全保存后git pull --ff-only返回Already up to date，apply后重新校验30个文件一致，再删除两份任务备份。仅复审本次差异，无未解决问题；同步后TypeScript、196项回归、实际H5/微信产物及链接、git diff --check复验通过，远端无相关代码变化。
