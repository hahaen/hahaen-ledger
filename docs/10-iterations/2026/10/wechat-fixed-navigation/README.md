# 微信顶部导航固定（2026-10-03）

- requirement：微信各页面顶部标语、标题及返回按钮保持原位置，正文上下滚动。范围为现有 NativeNavigation 的全部调用页。
- design：统一组件固定视口顶部并覆盖状态栏背景；沿用胶囊定位、色彩与字号。独立占位保留原有高度、顶部补偿与下方间距，固定层 z-index=9，低于系统弹窗；H5 无微信胶囊时保留原布局。
- database/api/backend：无变更，相关测试、迁移及服务验证不适用。
- frontend：NativeNavigation 四种变体统一接入固定层与占位，保留返回事件、禁用状态和胶囊右侧避让。未修改业务页及公共 SCSS；工作区其他任务的备注、日历修改不属于本轮。
- testing：实际 SFC 经 Vue 编译后在 Chrome 运行；320×568、375×667、430×932、667×375 四尺寸，六种导航配置，滚动到 300/600/0 时导航坐标保持不变，正文实际滚动、首段位置保持原值、返回按钮触发且无横向溢出。24 组 PASS，见 [几何证据](evidence/component-scroll.jsonl)。微信胶囊参数是隔离夹具，此验证不能代替微信手势。
- commands：`pnpm --dir app run typecheck` PASS；`node --test app/tests/*.test.mjs` 134/134 PASS；`pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin` PASS；`pnpm --dir app exec uni build -p mp-weixin --mode development --outDir dist/dev/mp-weixin` PASS。`PLAYWRIGHT_MODULE_PATH=<本机已安装Playwright路径> CHROME_EXECUTABLE_PATH=<本机Chrome路径> node app/tests/fixed-navigation-browser.cjs` 24 组 PASS。初次类型检查与浏览器依赖路径错误已修正并复跑。
- verification：PASS 为类型、回归、双端构建和隔离组件滚动。PARTIAL：微信工具已加载新组件，首页、我的、帮助页标题显示及胶囊对齐已观察；未取得微信实际滚动后坐标证据。BLOCKED：工具自动滚动/拖动连续返回 `noWindowsAvailable`，后续检测用户切换工具状态，停止争用；无可操作微信真机，微信手势滚动验收未完成。NOT_RUN：部署、发布。按 AGENTS.md 的必需平台验收约束，本轮暂不自动提交或推送；保留可检查的代码与证据。
- rollback：恢复 NativeNavigation 原实现并移除本轮浏览器验证脚本；无数据库和业务数据回退。

## 2026-10-03 用户授权电脑控制复测

已实际点击“我的”与“关于与帮助”进入帮助页，截图可见新标题栏。尝试窗口 Raise、应用路径及实际进程 com.github.Electron 绑定、滚轮与重置控制会话；滚轮仍立即返回 `noWindowsAvailable`。控制台点击/键盘/输入接口未产生可观察的输入结果，不能作为滚动证据。微信实际滚动验收仍为 BLOCKED；没有进行保存、发布或用户数据修改，没有把静态画面写为滚动 PASS。


## 2026-10-03｜用户明确要求提交当前改动

用户本次明确要求“帮我提交代码”，将微信顶部导航固定与日历主体滚动一并纳入提交范围；此前“暂不自动提交”的记录保留为历史状态，本次按明确提交指令执行。微信实际手势验收仍为 BLOCKED，未据此宣称平台运行验收完成。

提交前复验 PASS：`node --test app/tests/*.test.mjs`（134/134）、`pnpm --dir app run typecheck`、`pnpm --dir app run build:h5`、`pnpm --dir app run build:mp-weixin`；设置本机运行库和 Chrome 路径后执行 `node app/tests/fixed-navigation-browser.cjs`（24/24）与 `node app/tests/calendar-scroll-browser.cjs`（4/4视口）。微信生产 WXML 检查：唯一 scroll-view、无 catchtouchmove。浏览器证据使用只读夹具，无真实账务写入；无后端、数据库或 API 变更，未部署或发布。

同步检查 PASS：包含未跟踪文件的 stash 安全备份后，`git pull --ff-only` 返回 Already up to date；恢复后16个文件 SHA-256 全部一致，确认无代码或依赖变化后删除本次 stash。构建与浏览器运行证据对应同一份代码；同步后再次检查回归、类型和差异。
