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
PASS：本次展示逻辑、相关回归、类型检查、双端构建及只读视觉验证。无必需 FAIL/BLOCKED。H5 已发布成功（f716030，2026-10-30 14:09:17+08:00）；微信上传、提审和发布由用户独立完成，工具/真机运行 NOT_RUN。

## rollback
如需撤回，新增提交恢复 dateTitle 并重新发布 H5；无数据库回退操作，不创建旧应用备份或自动回滚。

## 远端同步与复审
`git stash push -u` 安全保存后执行 `git pull --ff-only`：Already up to date。恢复后逐文件 SHA-256 完全一致，才删除 stash。同步后复审本次唯一代码差异及文档，无未解决问题、冲突或无关待推送提交；代码和依赖未变，原双端构建证据仍适用。

## 发布结果
- 实现提交：`f716030 feat(app): 首页记账日期追加星期`，普通推送 GitHub `origin/main` 并核对远端一致。同步后重复现有后台刷新回归 22/22 PASS，文档链接检查 PASS。
- 主工作区出现并行任务的文档修改，首次发布入口在构建/上传前安全拒绝脏工作区，未修改服务器。随后用同一已提交版本的独立临时 worktree 发布，保留所有并行任务修改。
- `python3 deploy/local/release.py h5`：在干净独立 worktree 退出 0，重新安装锁定依赖、类型检查及生产构建 PASS；服务器输出 Published/Deployed ledger/h5 f716030。
- 服务器 h5-release.txt：f716030，14:09:17+08:00；公开 HTTPS HTML 和 6 个资源（含首页 JS）与本次构建逐字节一致，首页模块包含星期逻辑。Python urllib 初次验证受本机 CA 配置影响，改用系统 curl 正常证书校验后通过，未跳过 TLS 校验。
- 后端无需更新：运行版本仍为 `6980b29`，容器 running/0 重启；未执行 SQL、Migration 或配置变更。
- H5 已实际发布，刷新页面即可使用。微信命令行构建 PASS，未启动开发者工具、运行测试或上传发布；按当前阶段规则排除运行验收，用户如需使用需独立发版。
- 上传临时目录与 H5 incoming 目录均为空，旧静态站点已按发布脚本移除；未备份旧应用，数据库、文件、日志和运行密钥保留。

发布证据同步：远端新增 `1baf72d` 平台操作边界文档。安全 stash 后 ff-only 同步；三份审计文档尾部上下文冲突已按语义处理，保留远端完整平台规则记录，仅将本轮“待发布”改为实际成功结果。复审四份本次证据差异及冲突解决无未解决问题，文档检查 PASS；远端未改变业务源码，无需重复业务构建或发布。

## 星期视觉层级调整（2026-10-30 追加）

### requirement / design
按用户反馈，星期比日期更小、颜色更浅，彼此区分。日期保持 15px/700；星期独立 text，12px/400、灰绿色 #6b7572，复用 date-week 的 8px 左间距。仅首页作用域覆盖，不改其他日期组件。

### database / api / backend
无变化，不需要 Migration、SQL、配置或后端发布。

### frontend / Review
仅修改首页 dateTitle/weekdayTitle 与模板，以及 prototype.scss 首页 date-week 规则。星期保留数值构造日期的算法。已 Review 本次代码：嵌套 text 两端兼容、样式作用域、字重与间距、日期来源和金额布局均符合需求，无未解决问题；之后执行验证。

### testing / commands / verification
PASS：`node --test tests/page-flows.test.mjs tests/tab-background-refresh.test.mjs tests/responsive-layout.test.mjs` 67/67；`pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin` 均退出 0（仅既有 Sass 提示）。`node /tmp/ledger-home-weekday-style-qa.cjs` 使用本机后台 Chrome 与只读夹具，320/375/414 竖屏及 812 横屏均验证星期 12px/400、颜色 #6b7572、8px 间距、15px/700 日期、无重叠/溢出/运行错误。微信产物实际包含独立 date-week text 与对应样式。人工查看[375px 样式截图](h5-weekday-style-375.png) PASS。

H5 待发布；无必需 FAIL/BLOCKED。微信工具/真机及发布 NOT_RUN（按用户阶段规则排除，非必需），后端业务与数据库验证不适用。

### rollback
如需撤回，仅恢复本次模板/格式函数和首页样式覆盖，提交验证后重发 H5；不回退数据库、不自动回滚应用。

同步复审：安全 stash 后 `git pull --ff-only` 返回 Already up to date；逐文件 SHA-256 核对恢复一致后删除 stash。本次代码和文档再次 Review 无未解决问题，远端未改变源码/依赖；文档链接和 Git 空白检查 PASS。
