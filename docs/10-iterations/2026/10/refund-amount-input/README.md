# 微信退款金额输入误关闭修复

## requirement
2026-10-19：用户截图中的账单详情“填写退款金额”弹窗点击金额后消失，需要可编辑退款金额；完成后重新检查其他页面同类问题。

## design
本轮起始为 582b782，退款弹窗仍依赖祖先 click.self 关闭，微信产物没有隔离内部 tap；上轮已在微信工具实际点击退款输入框复现，本轮新增回归也在修复前失败。采用同级独立遮罩承载关闭与背景触摸拦截，面板相对定位并保留 H5 默认输入/滚动；保持现有退款布局、金额校验、幂等及提交状态。

## database
无数据库、Flyway、SQL、Entity 或数据变更，三方核对不适用，无需执行 SQL。

## api
沿用 POST /api/app/transactions/{id}/refunds，无协议、权限、金额、事务或幂等规则变化。

## backend
无后端改动，后端测试与构建不适用。本轮微信工具不确认退款，H5 请求由合成接口拦截，无真实账务写入；不宣称重新验证后端事务。

## frontend
退款遮罩移除 click.self 与祖先触摸拦截；独立同级 picker-touch-mask 保留 !saving 的关闭保护。refund-modal 增加 position:relative，H5 面板 touchmove 仅停止冒泡，不取消默认行为，小程序为 bindtouchmove。输入绑定、错误提示、取消/提交按钮和原有退款函数不变。

## testing

- PASS：新增模板事件回归修复前 2 FAIL，修复后 2/2 PASS；全量前端回归 178/178，0 跳过；TypeScript、H5/微信生产构建通过。
- PASS：微信 WXML 退款背景无关闭/取消默认触摸事件，独立同级遮罩有 catchtap/catchtouchmove，面板为 bindtouchmove，输入保留 bindinput。
- PASS：微信开发者工具 Stable 2.02.2608070、基础库 3.17.4、iPhone 12/13 (Pro) 模拟器：实际点击金额不再关闭，改为 12.34 后正文点击保持打开，取消与背景关闭正常；未确认真实退款，未保存真实账务截图。
- PASS：实际生产 H5 页面，以合成支出/收入数据覆盖 375×667、320×568、667×375、414×896，共 8 场景。验证标题与输入点击、金额编辑、取消/背景关闭、超额校验、提交期间禁用输入及按钮/禁止背景关闭、失败保留草稿、重试沿用同一幂等键且提交 1234 分、成功刷新退款记录。每场景 2 个合成 POST，全部拦截，不连接真实账务。
- PARTIAL：H5 合成接口和微信桌面模拟器不替代真机软键盘及生产验收。NOT_RUN（补充）：iOS/Android 微信、移动浏览器真机键盘、发布。

## commands

1. git status --short / branch -vv / remote -v：工作区与暂存区干净，main 跟踪 origin/main，HEAD 582b782，GitHub/Gitee 双推正常。
2. node --test app/tests/refund-modal-events.test.mjs：修复前 2 FAIL；修复后 2/2 PASS。
3. node --test --test-reporter=dot app/tests/*.test.mjs：178 项全部通过；pnpm --dir app run typecheck：退出 0。
4. pnpm --dir app run build:h5 / build:mp-weixin：退出 0，双端 Build complete，已有 Sass legacy-js-api 提示。
5. node app/tests/visual-server.mjs 启动 18761 本地服务；使用本机 Playwright/Chrome 路径执行 node app/tests/refund-modal-browser.cjs：最终 8 场景 PASS。首次脚本紧接超额输入后立即再填金额并提交，受 uni-app H5 useValueSync 的 100ms input 节流影响，等待提交态超时；核对本机运行库后，脚本明确等待 150ms 同步窗口，复审后全部通过。未因此修改业务代码。
6. Python 检查微信 detail.wxml 退款事件：PASS；其他 6 处历史面板仍是祖先 bindtap、内容无 catchtap。
7. Node Vue AST 全量扫描：36 个 SFC，16 个 scroll-view，取消默认触摸的 scroll-view 祖先为 0；剩余 9 处 click.self，2 处有内容保护、7 处无保护，其中退出登录在微信无打开入口，当前微信可达风险为 6 处。
8. CUA 操作当前微信产物完成上述实际金额输入、正文、取消和背景关闭验收，无业务提交。

## review
测试前仅审查本轮 detail.vue、prototype.scss、两个新回归文件及必要文档，核对事件边界/层叠、默认输入行为、禁用/关闭保护、金额绑定和 API 隔离，无未解决新增代码问题。浏览器脚本调整 input 同步窗口后重新 Review 并重跑。其他页面仅按用户要求审查同类事件结构，不修改历史业务代码。

## verification
PASS：本轮 Review、178 项前端回归、类型检查、双端构建、微信产物及模拟器输入、H5 合成接口 8 场景。PARTIAL / NOT_RUN：见 testing 的真机边界。无未解决本次新增代码 FAIL/BLOCKED。

更新范围：后端无需更新；微信小程序发布新包，H5 更新共享弹窗静态构建后生效。无需数据库、SQL、配置或额外图片操作，两个客户端无先后依赖，均由用户发版；本轮未发布。

## rollback
以新提交回退本轮前端代码及必要文档；已发布时重新构建并发布回退版本。无数据库、配置或历史账务回滚。

## 修复后其他页面复查

当前微信可达仍有以下 6 处同类风险，本轮仅检查而未修复。其根因是面板祖先 bindtap 关闭、内容没有 catchtap，点击输入或普通正文会冒泡；确认类这里只指误关闭，不等同于自动删除或权限问题。

| 页面/弹窗 | 源码位置 | 验证边界 |
| --- | --- | --- |
| 个人中心：修改密码 | app/src/pages/profile/profile.vue:333 | 源码和当前 WXML 确认；未输入或修改密码，实际输入待验 |
| 账单详情：删除账单/退款确认 | app/src/pages/detail/detail.vue:195 | 上轮微信点击删除账单标题已复现，本轮确认当前产物结构仍相同 |
| 账户详情：删除账户确认 | app/src/pages/account/account.vue:251 | 源码和当前 WXML 确认，实际点击待验 |
| 编辑记账：放弃修改 | app/src/pages/entry/entry.vue:288 | 源码和当前 WXML 确认，实际点击待验 |
| 待办编辑：放弃编辑 | app/src/pages/ha-todo-editor/ha-todo-editor.vue:194 | 源码和当前 WXML 确认，实际点击待验 |
| 待办详情：删除确认 | app/src/pages/ha-todo-detail/ha-todo-detail.vue:93 | 源码和当前 WXML 确认，实际点击待验 |

修改密码是剩余唯一输入型高优先级场景，其他 5 处主要为正文误关。退出登录确认(mine.vue:225) 为潜在同结构，但微信无正常打开入口，不计入 6 处。

新增/编辑账户有内部 catchtap，物品表单/确认、日期/月份、记账账户/日期/时间/备注弹窗及已修复的还款弹窗采用独立遮罩，未发现同类输入误关结构。新增/编辑账户的普通 view overflow 和祖先 catchtouchmove 仍需另做小屏/键盘滚动验证，不以本次静态审查宣称全平台滚动通过。

同类结构扫描 PASS；历史剩余问题为 PARTIAL，未逐项真机验收，不属于本次新增代码问题。本轮不自动修改其他弹窗或执行退款、删除、改密、发布。

## 同步后复审与验证

10 个任务文件 stash 保存后，git pull --ff-only 返回 Already up to date；抓取 Gitee 后两个远端与起始 HEAD 582b782 均无独有提交。stash apply 后逐文件 SHA-256 一致，无冲突，再 drop 备份。同步后复审仅覆盖本轮差异，无未解决新增问题；再次执行 178 项回归、类型检查及 H5 8 场景全部通过。源码/依赖未变化，既有双端构建、微信产物与工具输入验证继续对应最终代码。
