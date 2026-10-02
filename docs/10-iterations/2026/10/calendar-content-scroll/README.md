# 日历标题固定与主体整体滚动（2026-10-03）

- requirement：顶部“按日期回看每一笔生活”保持固定；月历、当日汇总、日期标题和全部账单一起上滑，解决只能看到一笔的问题。
- design：标题和底部导航位于滚动容器外；中间唯一原生 scroll-view，账单列表自然展开；小屏、高屏和横屏统一规则，保留原有色彩、卡片和日期交互。
- database/api/backend：无变更，不适用；不执行真实账务写入。
- frontend：调整 calendar.vue 与仅日历相关样式，保留月份弹窗、加载/空态/失败重试和刷新逻辑。
- testing：PASS：134/134前端回归、类型检查、H5/微信生产构建。H5生产只读夹具覆盖320×568、375×667、430×932、667×375触摸上滑/月历上移/标题与底栏不动/第12笔完整可见/无横向和页面溢出；五行与六行月历、日期选择、空态、错误重试、月份弹窗通过。微信生产WXML为唯一scroll-view，无主体catchtouchmove。
- evidence：[375px滚动后截图](evidence/h5-scrolled-375x667.png)；自动行为检查见 `app/tests/calendar-scroll-browser.cjs`，依赖既有只读 `visual-server.mjs`，不连接真实后端。
- commands：`node --test app/tests/*.test.mjs` → 134/134 PASS；`pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin` → PASS。`node app/tests/visual-server.mjs` 启动独立只读服务；设置本机 `PLAYWRIGHT_MODULE_PATH` 与 `CHROME_EXECUTABLE_PATH` 后执行 `node app/tests/calendar-scroll-browser.cjs` → 4视口PASS。首次Playwright默认浏览器缺失，改用已安装Chrome后通过。微信dev编译已完成，尝试工具界面验证未取得可靠滚动证据。
- sync：`git pull --ff-only` → Already up to date；同步后134/134回归、类型检查、H5/微信生产构建及4视口触摸运行复验PASS。按AGENTS.md，微信平台滚动验收BLOCKED，暂不提交/推送；保留本轮与已有无关改动。
- verification：代码、类型、生产构建与H5运行验证PASS；PARTIAL：微信构建产物已确认；BLOCKED：微信工具原生窗口操作noWindowsAvailable，AX操作后截图与焦点状态不足以证明滚动，必需微信平台运行验收未完成；NOT_RUN（补充）：手机微信、iOS Safari、部署/发布。数据库、API、后端、权限、金额、事务、幂等、Redis/MinIO不适用，业务逻辑与接口未改。
- scope：工作区已有共享导航相关改动，本次不纳入提交；运行验证基于当前工作区。
- rollback：恢复本轮日历模板及样式，无数据库回退。


## 2026-10-03｜用户明确要求提交当前改动

用户本次明确要求“帮我提交代码”，将微信顶部导航固定与日历主体滚动一并纳入提交范围；此前“暂不自动提交”的记录保留为历史状态，本次按明确提交指令执行。微信实际手势验收仍为 BLOCKED，未据此宣称平台运行验收完成。

提交前复验 PASS：`node --test app/tests/*.test.mjs`（134/134）、`pnpm --dir app run typecheck`、`pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin`；设置本机运行库和 Chrome 路径后执行 `node app/tests/fixed-navigation-browser.cjs`（24/24）与 `node app/tests/calendar-scroll-browser.cjs`（4/4视口）。微信生产 WXML 检查：唯一 scroll-view、无 catchtouchmove。浏览器证据使用只读夹具，无真实账务写入；无后端、数据库或 API 变更，未部署或发布。

同步检查 PASS：包含未跟踪文件的 stash 安全备份后，`git pull --ff-only` 返回 Already up to date；恢复后16个文件 SHA-256 全部一致，确认无代码或依赖变化后删除本次 stash。构建与浏览器运行证据对应同一份代码；同步后再次检查回归、类型和差异。
