# H5 主屏幕应用图标修复

## requirement
2026-10-24：用户从 iPhone Safari 将哈记账添加到主屏幕时，图标显示灰色“哈”字。目标是复用已有品牌图标，修正 H5 资源链路；不发布服务。

## design
线上 HTML 的 apple-touch-icon 指向 /static/brand-white.png，实测 HTTP 404；应用部署在 /haji/。已有白底图位于 app/offloaded-static-assets/wx/brand-white.png，H5 入口直接引用此存在的源文件，由 Vite 生成带 base 和内容哈希的资源链接；不把图片重新放回小程序 static 目录。图片原样复用，不改品牌设计或业务页面。

## database
无数据库、Flyway、SQL、Entity 或数据变更，不适用三方核对，无需执行 SQL。

## api
无 API、认证、权限、金额、事务或幂等变化。

## backend
无后端实现、配置或依赖变化，后端测试和构建不适用。

## frontend
仅修正 app/index.html 的主屏幕图标源路径；浏览器 favicon 保持现有资源。回归从实际 H5 生产 HTML 解析图标并通过 /haji/ 本地 HTTP 服务请求，校验状态、MIME、PNG 文件头和与品牌源文件字节一致。

## testing

- PASS：新增生产产物回归先在旧产物上失败（apple-touch-icon 缺少 /haji/ 前缀），修复重建后两个图标均 HTTP 200、image/png、PNG 文件头与正方形尺寸正确，并与各自源图字节一致。主屏幕图为 /haji/assets/brand-white-CWQ4hh1_.png。
- PASS：全量前端回归、TypeScript、H5 与微信生产构建均退出 0。微信产物不包含 brand-white 或 offloaded-static-assets；仅 H5 资源受到影响。
- PASS：实际 DEV 服务 http://127.0.0.1:5180/haji/ 返回页面，解析其主屏幕图链接并请求得到 HTTP 200 image/png，与源图一致；验证后停止本轮启动的服务。
- PARTIAL：本轮完成构建/DEV 与生产产物 HTTP 资源链路验证，不等同于 iPhone 系统 UI 验收。
- NOT_RUN（发布后补充）：用户发布完整 H5 后，在 iPhone Safari 刷新页面、删除旧桌面入口并重新添加，确认图标与启动正常。本轮不发布。无未解决的本轮必需 FAIL/BLOCKED；后端/数据库及微信真机业务因未受影响不适用。

## commands
1. git status --short / git diff --cached --stat / git branch -vv / git remote -v，起始工作区和暂存区干净，main 跟踪 origin/main，HEAD 80914aa，origin 包含 GitHub/Gitee 双推地址。
2. curl 读取线上 /haji/ HTML；旧图标 /static/brand-white.png 为 HTTP 404 text/html，favicon 为 HTTP 200 image/png。

3. node app/tests/h5-icons-build.check.mjs：旧产物 favicon PASS，主屏幕图缺少部署前缀而退出 1；修复并生产重建后两图 PASS，退出 0。
4. node --test --test-reporter=dot app/tests/*.test.mjs、pnpm --dir app run typecheck：均退出 0。
5. pnpm --dir app run build:h5、pnpm --dir app run build:mp-weixin：均 Build complete、退出 0；仅有已有 Sass legacy-js-api 弃用提示及 uni-app 更新提示。微信构建最终完成状态已再次执行确认，无平台源码改动。
6. Python 遍历微信构建目录，断言不存在 *brand-white* / *offloaded-static-assets*：PASS。
7. pnpm --dir app run dev:h5 --host 127.0.0.1：5180 正常启动；Python 实际请求 /haji/ 并解析图标链接，请求与源文件字节比对 PASS。验证后 Ctrl-C 主动停止，退出 130 为人工终止而非启动失败。
8. git diff --check：退出 0。

## review
PASS：执行验证前，仅 Review 本轮新增的生产产物回归脚本，再执行旧产物失败验证；修复后仅 Review HTML 一处路径替换、回归脚本和必要文档。确认引用已跟踪源图、Vite 可识别 apple-touch-icon 并处理 base/哈希、未新增小程序 static 文件；脚本仅临时监听 127.0.0.1 并在 finally 关闭服务，检查实际产物链接/状态/MIME/PNG 内容，不使用合成业务数据。无页面/API/认证/权限/金额/事务/幂等变化，无真实配置/Secret/构建产物纳入提交，无未解决本轮问题。

## verification
PASS：本轮实现与验证结果见 testing；同步后复审和生产产物回归复验均通过。更新范围：仅需用户重新构建并发布 H5 静态产物（包含新哈希图标），后端与微信小程序无需更新，无 SQL/环境配置操作。发布后删除旧桌面入口，刷新 Safari 再重新添加；本轮不执行发布。

## rollback
以新提交回退本次 HTML、回归和文档，重新构建并由用户发布 H5；无数据库及用户数据回滚。

## 同步后复审与验证

8 个任务文件通过精确路径 stash -u 保存；git pull --ff-only 返回 Already up to date，git fetch gitee main 成功，双向 log 确认 GitHub/Gitee 与本地均无独有提交。stash apply 恢复后 8 文件 SHA-256 全部一致，再删除 stash 备份。同步后只复审本轮 HTML、回归脚本及文档差异，资源/平台/权限边界与前述一致，无冲突、无未解决问题；再次执行生产图标 HTTP 回归，两图 PASS，git diff --check 通过。远端未改变代码或依赖，已通过的全量回归、类型检查、双端构建和 DEV 验证仍对应相同源码，无需重复全量构建。
