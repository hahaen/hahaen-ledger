# 首页最近记账日期补充星期（2026-10-30）

## requirement
按用户截图，在首页最近记账日期后追加“周几”，例如“2026年10月30日 周五”。

## design
保留日期样式、分组及右侧收支布局；星期取当前分组日期，不能取今天。

## database / api / backend
均无变化，不执行 Migration、SQL 或后端发布。

## frontend
仅修改 `app/src/pages/index/index.vue` 的 dateTitle。将年月日拆成数值后构造本地 Date，使用 getDay 映射周日到周六；不解析日期字符串，避免 UTC 日期偏移及平台字符串解析差异。H5、微信共用。

## Review
已 Review 本次唯一代码差异，核对截图需求、日期来源、周日索引、时区及 H5/微信兼容性，无未解决问题。无 API、权限、金额、事务或幂等逻辑变化。先 Review 再验证。

## testing
PASS：67 项现有相关回归；10 个星期/跨月/跨年/闰日样例 × 上海、洛杉矶、UTC 三个时区，共 30 项断言；TypeScript 检查、H5/微信生产构建；本机 Chrome 后台只读夹具 320×568、375×812、414×896、812×375 验证日期/星期、标题与收支不重叠、无横向溢出及页面运行错误。已人工查看 [375px 截图](h5-375.png)。

本次为日期展示，不新增镜像实现的永久测试。真实 API 账务、后端测试及数据库验证不适用；微信工具/真机运行 NOT_RUN，构建不能替代真机。

## commands
- `cd app && node --test tests/page-flows.test.mjs tests/tab-background-refresh.test.mjs tests/responsive-layout.test.mjs`：67 PASS，0 FAIL。
- `cd app && pnpm run typecheck`：退出 0。
- `cd app && pnpm run build:h5 && pnpm run build:mp-weixin`：双端 Build complete，退出 0；存在既有 Sass legacy-js-api 提示。
- Node 临时脚本从实际 dateTitle 声明转译，核对十组已知日期及三个时区：30 断言 PASS。
- `node app/tests/visual-server.mjs` 配合 `node /tmp/ledger-home-weekday-qa.cjs`：本机已安装 Chrome headless，只读夹具，四种尺寸均 PASS，不连接真实后端。
- `git diff --check`：退出 0。

## verification
PASS：本次展示逻辑、相关回归、类型检查、双端构建及只读视觉验证。无必需 FAIL/BLOCKED。H5 发布待执行；微信上传、提审和发布由用户独立完成，工具/真机运行 NOT_RUN。

## rollback
如需撤回，新增提交恢复 dateTitle 并重新发布 H5；无数据库回退操作，不创建旧应用备份或自动回滚。

## 远端同步与复审
`git stash push -u` 安全保存后执行 `git pull --ff-only`：Already up to date。恢复后逐文件 SHA-256 完全一致，才删除 stash。同步后复审本次唯一代码差异及文档，无未解决问题、冲突或无关待推送提交；代码和依赖未变，原双端构建证据仍适用。
