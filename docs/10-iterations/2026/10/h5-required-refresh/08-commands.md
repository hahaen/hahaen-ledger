# 真实命令

开始前：git status --short（空工作区）、git branch --show-current（main）、git remote -v / git rev-parse --abbrev-ref --symbolic-full-name @{upstream}（GitHub origin/main）。

- `cd app && pnpm exec node --test tests/h5-update.test.mjs`：7/7通过。
- `cd app && node --test tests/*.test.mjs`：194/194通过，输出临时存于 /tmp/haji-h5-update-tests.log。
- `pnpm --dir app run typecheck`、`pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin`：均退出0。原生按钮修改后双端复跑通过；原生cancel最终修改后类型/H5复跑通过。
- `node app/tests/wechat-share-build.check.mjs`：25/25生产页面通过，欢迎图未进入小程序包。
- Python实际产物检查：版本JSON与H5 JS匹配，微信无版本文件和H5更新代码。
- 本地Python静态服务5188承载真实H5产物，版本探针可切换同版本/不同版本/503；Chrome在后台以可见性模拟验证，不操作现有用户页。截图/尺寸见evidence目录。原生刷新按钮与cancel事件的初验问题修复后复验通过。鼠标点击工具一次超时，改用原生Enter完成失败/重试路径验证。

最终补强后194/194回归、TypeScript、H5/微信构建再次PASS。实际两次生产构建（f18ed7d9 → 52c10ef5）在同源页面触发更新后，Enter整页刷新至新构建且无重复弹窗；保留隐私页hash，见 evidence/refresh-complete.png。临时存储探针、可见性模拟和尺寸覆盖均清理，未操作用户既有生产标签页。

同步与复审：验证后使用含未跟踪文件的独立stash安全保存，git pull --ff-only返回Already up to date；apply后31个文件SHA-256逐字节一致，再删除备份。仅复审同步后的本次代码及文档差异，无未解决问题；复跑类型检查、7项定向回归及H5产物一致性、新增12个链接检查、git diff --check均PASS。完整194项及双端构建在同步前最终代码上通过，远端无变更。
