# 微信还款金额输入误关闭修复

## requirement
2026-10-19：微信小程序还款主弹窗点击金额输入框后消失，无法修改还款金额。需保持输入、选择账户、取消与背景关闭行为正确。

## design
在微信开发者工具现有生产产物中复现：打开还款表单，点击金额输入框，弹窗立即关闭。生产 WXML 的主遮罩包含 bindtap，JS 关闭处理直接修改 repayOpen，没有保留源码 click.self 的过滤。此时输入框没有 blur 或键盘关闭业务处理，关闭来自祖先点击冒泡。

只将主弹窗关闭和背景触摸拦截移到同级独立遮罩，面板使用 position:relative 保证位于遮罩上方；复用现有 H5 仅停止冒泡的触摸函数并更名为 onRepayModalTouchMove。账户选择弹窗只同步函数名，保留已验证的滚动结构。

## database
无数据库、Flyway、SQL、Entity 或数据变更；数据库核对不适用，无需执行 SQL。

## api
沿用账户和还款 API，无权限、金额、事务、幂等或协议变化。

## backend
无后端改动，后端构建和账务事务联调不适用；本轮不确认还款，不写真实账务数据。

## frontend
主表单根节点不绑定点击关闭或禁止触摸；独立遮罩保留 !saving 关闭保护、catchtap 和 catchtouchmove。输入框仍绑定 repayAmount，保留金额校验及提交禁用状态。正文点击不会关闭，取消和背景关闭仍有效，选择资金账户后草稿金额保留。

## testing

- PASS：新增模板回归修复前 1 项失败，修复后全量前端 176/176 通过，0 跳过；TypeScript 和 H5/微信生产构建通过。
- PASS：生产 WXML 主遮罩/面板祖先均无点击关闭，独立触摸遮罩有 catchtap，金额保留 bindinput。
- PASS：微信开发者工具 Stable 2.02.2608070、基础库 3.17.4、iPhone 12/13 (Pro) 模拟器：原始点击金额误关闭已复现；新产物点击金额保持打开，可编辑为 12.34，选择资金账户后返回仍为 12.34，点击背景及取消均关闭。未点确认还款，没有业务写入，真实账务截图不保存到仓库。
- PASS：生产 H5 合成接口四视口 375×667、320×568、667×375、414×896，点击标题/金额输入后保持弹窗、修改草稿、选账户后金额保留、取消/主遮罩关闭；同时回归列表上下触摸滑动、末项选择及横屏返回按钮。零业务写请求。
- PARTIAL：H5 使用合成接口，微信使用桌面工具模拟器；不等同于手机原生键盘、软键盘遮挡和发布后验收。
- NOT_RUN（补充）：iOS/Android 真机键盘及触摸验收、生产发布。本轮无未解决必需 FAIL/BLOCKED。

## commands

1. git status --short / branch -vv / remote -v：工作区和暂存区干净，main 跟踪 origin/main，起始 f97c1ad，GitHub/Gitee 双推配置正常。
2. node --test app/tests/repayment-picker-scroll.test.mjs：修复前 2 PASS / 1 FAIL，失败定位主弹窗祖先 click.self 关闭。
3. node --test --test-reporter=dot app/tests/*.test.mjs：176 项全部通过；pnpm --dir app run typecheck：退出 0。
4. pnpm --dir app run build:h5 / build:mp-weixin：两端 Build complete，退出 0；已有 Sass legacy-js-api 弃用提示，无构建错误。
5. Python 检查生产 account.wxml 的主弹窗/遮罩事件及 bindinput：PASS。
6. node app/tests/visual-server.mjs 启动 18761 本地生产夹具服务；PICKER_TEST_URL 指向 /haji/，使用本机 Playwright 和 Chrome 运行 node app/tests/repayment-picker-browser.cjs：四视口全部 PASS。
7. CUA 操作现有微信项目完成上述输入、选择、背景关闭及取消验收。一次原生通道断开后重连并分别重新核对关闭状态，最终均通过，无真实账务写入。

## review
仅审查本轮 account.vue、prototype.scss、两个回归文件及必要文档。测试前核对事件边界、微信编译产物语义、遮罩层叠、输入框绑定、选择状态、保存禁用和 H5 复用性，无未解决问题；金额、权限和事务逻辑未改。范围不扩展到其他历史弹窗。上一轮只验证账户选择列表，未覆盖主弹窗金额点击，本轮补齐回归。

## verification
PASS：本轮 Review、176 项回归、类型检查、双端构建、生产 H5 四视口和微信模拟器输入/选择/关闭。PARTIAL / NOT_RUN：见 testing 的真机边界。最终无未解决必需 FAIL/BLOCKED。

更新范围：后端无需更新；微信小程序需要发布新包以修复金额输入，H5 同步共享遮罩实现需发布新静态构建。无数据库、SQL、配置或额外图片资源操作，两客户端无先后依赖；由用户自行发版，本轮未发布。

## rollback
以新提交回退本次前端及文档；已发布时重新构建并发布回退版本。无数据库、SQL、配置回滚，不修改历史账务数据。

## 追加分析：其他微信页面同类风险

用户追加要求分析其他页面；本段仅排查，未修改其他业务弹窗。扫描全部 36 个 Vue SFC 及 16 个 scroll-view 模板，并核对当前微信生产 WXML：剩余 10 处 click.self 遮罩，2 处有内容 catchtap 保护，8 处缺少保护，其中“我的/退出登录”的打开按钮只在 H5 编译，当前微信可达为 7 处。

| 页面/场景 | 源码位置 | 实际影响 | 证据 |
| --- | --- | --- | --- |
| 账单详情：填写退款金额 | app/src/pages/detail/detail.vue:175 | 点击金额输入框关闭，无法正常编辑 | 微信模拟器实际点击复现；未提交退款 |
| 个人中心：修改密码 | app/src/pages/profile/profile.vue:333 | 输入框或显隐按钮点击冒泡会关闭，干扰密码编辑 | 源码 + 生产 WXML；未输入/修改密码，运行待验 |
| 账单详情：删除账单/退款确认 | app/src/pages/detail/detail.vue:188 | 点击标题/正文误关闭 | 微信模拟器点击“删除这笔账单”标题复现；未确认删除；退款共用同一面板 |
| 账户详情：删除账户确认 | app/src/pages/account/account.vue:251 | 点击标题/正文误关闭 | 源码 + 生产 WXML，运行待验 |
| 编辑记账：放弃修改确认 | app/src/pages/entry/entry.vue:288 | 点击提示正文误关闭，相当于继续编辑 | 源码 + 生产 WXML，运行待验 |
| 待办编辑：放弃本次编辑 | app/src/pages/ha-todo-editor/ha-todo-editor.vue:194 | 点击提示正文误关闭 | 源码 + 生产 WXML，运行待验 |
| 待办详情：删除确认 | app/src/pages/ha-todo-detail/ha-todo-detail.vue:93 | 点击提示正文或错误说明误关闭 | 源码 + 生产 WXML，运行待验 |

上述 7 处生产 WXML 均为祖先 bindtap 关闭、内容无 catchtap。确认类弹窗这里只证明标题/正文误关，不推断确认操作必然失败、自动删除或越权。优先处理退款金额和改密两个输入场景，其次处理确认弹窗；可采用与本轮一致的独立遮罩模式，或在结构适配后复用 CenterModal。

“我的/退出登录确认”(mine.vue:225) 为相同潜在结构，但微信无正常打开入口，不计入当前 7 处。新增账户(assets.vue:186)、编辑账户(account.vue:238) 内容具有 click.stop/生产 catchtap，不属于此次点击误关；它们仍有祖先 catchtouchmove 和普通 view overflow 布局，小屏/键盘下滚动属于另外的待运行风险，不能据静态判断宣称滑动通过。

CenterModal 及物品新增/编辑/退役/删除/重新服役、MonthPicker、EntryDateTimePicker、记账账户/日期/时间/备注弹窗均已隔离背景关闭事件，未发现同类关闭祖先结构；16 个显式 scroll-view 未发现取消默认触摸的模板祖先。此结论是本次事件结构审查，不代表全应用真机兼容验收通过。

分析状态：扫描与产物核对 PASS；历史问题保留 PARTIAL（2 处运行复现、其他待运行），不属于本次还款修复新增问题，未擅自扩展修改范围。本轮使用 CUA 完成安全点击，不执行退款、删除、改密或发布。

## 同步后复审与验证

10 个任务文件在 stash 前后 SHA-256 一致，apply 确认恢复后再 drop。git pull --ff-only 返回 Already up to date；fetch gitee main 后两远端与起始 HEAD f97c1ad 均为 0/0 独有提交，无冲突。同步后仅本轮差异复审，无未解决新增问题；176 项回归、类型检查及生产 H5 四视口再次通过。源码及依赖未改变，已有双端构建和微信工具验收继续对应最终代码。历史同类风险按用户要求仅分析，未混入本轮修复。
