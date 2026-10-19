# 手机还款账户列表滚动修复

## requirement
2026-10-19：用户截图中的“选择还款账户”列表在手机 H5 和微信小程序无法滑动；应能浏览、选择所有有效资金账户，并返回还款表单。

## design
真实账户页面以 12 个合成资金账户复现 H5 触摸失败：375×667 下末项滑动前后 Y 坐标均为 1234.296875。单独让面板停止事件冒泡、不取消默认行为后，两个竖屏通过，横屏暴露返回按钮超出屏幕。

根因是祖先 `@touchmove.stop.prevent` 取消列表默认滚动，以及普通 view 无法提供微信原生纵向滚动。修复采用独立遮罩、原生 scroll-view 和受可用高度约束的 flex 列表，保持标题、返回按钮和现有卡片视觉。

## database
无 Schema、Flyway、SQL 或数据修改，无需执行迁移；数据库三方核对不适用。

## api
沿用账户查询和还款接口，无请求、响应、权限、金额、事务或幂等契约变化。

## backend
无后端改动，后端测试与构建不适用；本轮不重新宣称验证账务事务。

## frontend

- account.vue：背景禁止触摸及关闭事件移到与面板同级的遮罩；H5 面板仅停止冒泡，小程序使用 bindtouchmove，不生成祖先 catchtouchmove。
- 账户列表使用 scroll-view scroll-y；内部独立 flex 布局保留卡片间距，卡片不压缩。
- 面板最大高度取遮罩可用高度，列表可收缩，标题和返回按钮不收缩；移除面板自身滚动和旧 vh/dvh 高度覆盖，避免双层滚动及横屏裁切。
- 账户选择、返回时金额保留、点背景关闭和提交禁用规则不变。

## testing

- PASS：新增模板事件边界回归 2/2；连同账户详情定向测试 6/6，全量前端回归 175/175。
- PASS：TypeScript 检查及 H5、微信生产构建；已有 Sass legacy-js-api 提示，无构建错误。
- PASS：实际生产 H5 页面四视口 375×667、320×568、667×375、414×896，以 CDP 真实触摸事件验证上下滑动、末项可达且可选、返回按钮可见、金额保留、内容点击不误关、遮罩关闭、背景无位移、无水平溢出、零业务写请求。[375px 合成数据截图](evidence/h5-375.png) 已查看；测试禁止外部图像请求，图标未加载不作为视觉验收结论。
- PASS：微信生产 WXML 存在 scroll-view scroll-y，catchtouchmove 仅在独立遮罩，面板为 bindtouchmove。
- PASS：微信开发者工具 Stable 2.02.2608070 / 基础库 3.17.4 / iPhone 12/13 (Pro) 模拟器加载本次生产产物；通过资产→信贷账户详情→还款→选择账户，滚轮滚至列表末端，选中末项后回表单并取消。仅验证读取与本地选择，未确认还款，不记录真实账务截图或账号数据。
- PARTIAL：H5 使用合成接口；微信工具只确认原生列表滚轮滚动和选择，鼠标拖拽未形成有效触摸位移，不能替代真机手指滑动。
- NOT_RUN（补充）：iOS/Android 手机微信、手机 Safari/Chrome 触摸验收、发布后的真实页面；本轮不执行发布。

## commands
1. git status --short / git branch -vv / git remote -v：初始工作区、暂存区干净，main 跟踪 origin/main，HEAD 2e77512；origin 为 GitHub 拉取、GitHub/Gitee 双推。
2. pnpm --dir app run dev:h5 --host 127.0.0.1：启动 5180/haji 本地开发入口。
3. node app/tests/repayment-picker-browser.cjs（指定本机 Playwright 与 Chrome 路径）：修复前触摸位移断言失败；单变量隔离祖先事件后竖屏通过、横屏按钮可见性失败；最终开发页四视口通过。
4. node --test app/tests/repayment-picker-scroll.test.mjs：修复前 2 项失败，分别为普通 view 和祖先事件；修复后与 account-detail-fixes.test.mjs 联跑 6/6 PASS。
5. node --test app/tests/*.test.mjs：175/175 PASS，0 failures/skipped。
6. pnpm --dir app run typecheck / build:h5 / build:mp-weixin：全部退出 0，两端 Build complete。
7. node app/tests/visual-server.mjs：启动 18761 生产产物本地夹具服务；PICKER_TEST_URL 指向其 /haji/，运行 repayment-picker-browser.cjs 四视口 PASS；补充下滑与遮罩/内容点击断言后重新 Review 并重跑，四视口仍 PASS。
8. Python 检查微信 WXML 原生容器与遮罩事件：PASS。
9. 微信 CLI islogin：服务端口关闭，CLI 不可用；未修改安全设置，改用 CUA 操作已打开的项目完成上述模拟器验证。UI 操作及截图仅用于当前验证，不写入仓库。

## review
仅审查本轮 account.vue、prototype.scss、新增模板回归和浏览器脚本及必要文档；核对用户需求、事件默认行为/冒泡、微信编译语义、滚动高度、卡片收缩、选择/关闭/禁用状态和测试隔离。首次 Review 在测试前完成，无未解决代码问题；增强脚本时发现遮罩关闭后不应继续访问还款表单，调整用例顺序并复审后再执行。未扩展到无关历史实现，后端金额和权限逻辑未改。

## verification
PASS：本轮审查、175 项前端回归、类型检查、双端构建、生产 H5 触摸运行、微信产物和模拟器滚轮/选择验证。
PARTIAL / NOT_RUN：见 testing 的真实平台边界。无未解决必需检查 FAIL 或 BLOCKED；CLI 入口不可用已通过 UI 路径完成工具验证。

更新范围：后端无需更新；H5 需发布新静态构建，微信小程序需发布新包，两者包含本次滚动修复，彼此无更新先后依赖。无需数据库、SQL、配置或额外图片资源操作。均由用户自行发布，本轮未发布。

## rollback
通过新提交回退本轮前端与必要文档；已发布时重新构建并发布回退版本。无数据库、SQL 或配置回滚，不修改历史账务数据。

## 同步后复审与验证

`git stash push --include-untracked` 安全保存 11 个本轮文件后，`git pull --ff-only` 返回 Already up to date；`git fetch gitee main` 后 GitHub/Gitee main 均与本地 HEAD 2e77512 一致，双方无独有提交。`git stash apply` 恢复后逐文件 SHA-256 一致，无冲突，再删除 stash 备份。同步后只针对本轮差异重新 Review，无未解决问题；175 项前端回归、类型检查、生产 H5 四视口触摸回归再次通过。相关源码及依赖未变化，已完成的双端构建与微信 UI 验证仍对应最终源码，无需重复构建；未创建账务数据。
