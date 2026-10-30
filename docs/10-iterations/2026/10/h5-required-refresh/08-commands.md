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

- `git commit -m 'feat(app): 新增H5发版强制刷新弹窗'`：9309fd6；`git push origin HEAD:main`成功，`git ls-remote origin refs/heads/main`等于本地HEAD完整值9309fd696b39b1b768392d147359d3787600fcd1。
- `python3 deploy/local/release.py h5`：退出0；本机依赖、类型、H5生产构建通过，服务器HTTPS检查通过，发布9309fd6并移除旧站点。
- 公开验证：本机Python urllib证书信任链缺失，未关闭TLS校验，改用系统curl默认TLS校验通过；HTML、5个静态资源、haji-version.json与本机产物逐字节一致，版本8115d1f3-a80b-4b74-9e2b-a6e488f729f4存在于入口JS；未登录账户API返回401。
- SSH只读核对：h5-release/latest-release记录9309fd6，时间22:13:57+08:00；backend-release及容器OCI revision仍为6980b2956a35f594187327d42ae36ae1f58e8650，running/0重启；无haji.incoming.*及ledger-upload.*临时目录。

发布证据仅文档Review：范围为本次7份Markdown及production-check.json，对照实际命令与服务器结果无未解决问题；7个迭代内链接与git diff --check通过。无源码变化，业务测试和发布不适用。
