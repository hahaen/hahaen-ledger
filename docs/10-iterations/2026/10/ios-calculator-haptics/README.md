# iPhone Safari 金额键盘轻触反馈

## requirement
2026-10-25：仅为新增/编辑记账的金额键盘补充 iPhone Safari 网页轻触反馈，保留安卓 H5 与微信原有反馈，不启用全局振动，不发版。

## design
H5 专用适配：检测 iPhone Safari 与原生 switch 支持，透明原生 checkbox switch 覆盖单个按键，由手指直接触发系统触感；原按钮与覆盖层互为同级，不嵌套交互控件。保留原按钮键盘/读屏语义、禁用与逐次单一业务入口。减少动态效果时不捕获覆盖层点击。系统反馈由 Safari/iOS 决定，不能控制强度/时长，也不能由桌面仿真证明物理震感。

依据：[WebKit Safari 18 开关触感](https://webkit.org/blog/15865/webkit-features-in-safari-18-0/#switch)、[直接触摸适配示例与限制](https://github.com/m1ckc3s/project-fathom)。实现为项目独立适配，不添加第三方运行依赖。

## database
无 Schema、Flyway、SQL、Entity 或业务数据变化，不适用数据库三方核对。

## api
无 API、认证、权限、金额计算、事务或幂等契约变化；原保存入口及防重复提交继续使用。

## backend
无后端、Redis、MinIO、配置或日志变化，后端测试与构建不适用。

## frontend
新增 H5 专用 CalculatorKey 与 iosCalculatorHaptics 探测；通过 Vue h 创建原生 button/input，绕开 uni-button/uni-input 转换。仅 H5 条件编译包含适配；微信源码移除 H5 块后与基线逐字一致，构建不含新组件。视觉、字号、布局沿用原型和本次用户截图，原生按钮以低优先级 CSS 重置以消除 Safari 默认外观。

## testing
- PASS：全量前端 180/180，定向键盘/平台反馈/能力检测 9/9；TypeScript、H5/微信生产构建通过。
- PASS：最终 H5 生产包在 Chromium 与 WebKit 各 320×568、390×844、844×390 共 6 场景，以及 2 个非 iPhone 回退场景通过。覆盖 18 个真正 HTML input、直接触摸命中、单次数字/小数/退格/运算、Enter、减少动态效果、保存中全禁用、确定/再记各一次写请求、失败保留金额和同幂等键重试；额外检查按钮无原生边框/灰底、字号和确认底色。写请求由测试拦截，无真实账务写入。
- PASS：原有生产 H5 四视口×新增/三类编辑，共 16 组触摸滚动、键盘固定、备注入口回归。最终样式重置只移除原生控件默认外观，6 组最终浏览器场景复核布局无横向溢出。
- PASS：本轮 DEV 服务在 127.0.0.1:5180/haji 启动，WebKit iPhone UA 直接触摸 1、空格输入 2，算式恰好为 12，18 个覆盖层、无页面异常；接口仅通过本地只读夹具，不使用真实身份。[最终截图](evidence/dev-webkit.png) 中 2 的蓝框是键盘焦点，非触摸常驻样式；人工检查已消除灰底/边框且网格、配色保留。
- PASS：微信构建不含 CalculatorKey、iosCalculatorHaptics 或覆盖层标记，仍有 button 和 vibrateShort；未修改微信业务接线。
- PARTIAL：桌面 Chromium/WebKit 移动视口和本地夹具可以证明事件、样式与构建链路，不能证明 iPhone 物理震感或 VoiceOver 真机表现。
- NOT_RUN（发布后补充）：用户在 iOS 18+ Safari 更新后的页面实测每个键的系统短触感、VoiceOver 及系统触感设置影响。本轮未持有 iPhone 真机，不将能力检测或浏览器事件当成触感 PASS。无未解决本轮必需 FAIL/BLOCKED。

## commands
1. git status --short / git branch -vv / git remote -v：起始工作区和暂存区干净，main 跟踪 origin/main，HEAD c29cce9，origin 双推 GitHub/Gitee。
2. node --test app/tests/ios-calculator-haptics.test.mjs app/tests/entry-touch.test.mjs app/tests/tap-feedback.test.mjs：9/9；node --test --test-reporter=spec app/tests/*.test.mjs：最终 180/180，退出 0。首次新增 Mac UA 夹具写错，修正复审后通过。
3. pnpm --dir app run typecheck：初版及原生 button 修正后退出 0；pnpm --dir app run build:h5、pnpm --dir app run build:mp-weixin：Build complete、退出 0。H5 最终 CSS 修正后已重建。仅已有 Sass legacy-js-api 与 uni-app 更新提示，未升级依赖。
4. PLAYWRIGHT_MODULE_PATH=/Users/hahaen/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright CHROME_EXECUTABLE_PATH='/Applications/Google Chrome.app/Contents/MacOS/Google Chrome' WEBKIT_EXECUTABLE_PATH=/Users/hahaen/Library/Caches/ms-playwright/webkit-2370/pw_run.sh node app/tests/ios-calculator-haptics-browser.cjs：最终 8 场景 PASS、退出 0。先启动 node app/tests/visual-server.mjs。默认运行库期待 webkit-2336 未安装，改用本机已有 webkit-2370 后实际运行通过，无下载或安装。
5. 同一 PLAYWRIGHT_MODULE_PATH/CHROME_EXECUTABLE_PATH 执行 node app/tests/entry-scroll-browser.cjs：16/16、退出 0。
6. pnpm --dir app run dev:h5 --host 127.0.0.1：5180 启动；本地临时 WebKit 脚本以只读夹具替代 /api/app/**，触摸/空格输入与截图检查退出 0。初次夹具误拦截依赖 style/api 与 devtools-api 路径，出现 404/超时，收窄到 /api/app/** 后解决，无应用修补；未依赖真实后端。测试数据和会话仅为夹具。
7. Python 对微信平台源码去 H5 块后与 HEAD 基线逐字比较通过；遍历生产微信包无适配组件/探测/覆盖标记，WXML 保留按钮且工具含 vibrateShort：PASS。
8. git diff --check：退出 0；同步及最终文档检查见下。

## review
初审 PASS（测试前）：仅审阅本轮 CalculatorKey.vue、iosCalculatorHaptics.ts、entry.vue 的 H5 接线、两份新增回归及必要文档。核对平台条件编译、真实 input 创建、同级交互控件、单一保存入口、禁用与失败重试、键盘/读屏标签、减少动态效果、现有布局与无全局反馈；测试仅请求本地只读夹具，写请求由浏览器拦截，未连接真实账务。未发现未解决的本轮代码问题；无数据库/API/认证/事务修改。初次回归发现 Mac UA 夹具只替换了第一个 iPhone，仍包含 CPU iPhone 字样；已换成真实结构的 Mac UA，复审此测试修改符合排除 Mac 的目标，无实现变化。生产浏览器回归发现模板 button 被转换为 uni-button，Enter 不会激活；本轮组件改以 h 创建真正 HTML button，保留浏览器 Enter/空格/disabled 原生语义。已先复审此次组件修改：属性/slot 逐次传入、type=button 防隐式提交、点击仍单一 activate 入口，无嵌套控件或重复事件。复审无未解决问题后重跑受影响验证。浏览器脚本复审补充：WebKit 启动允许显式指定本机已有执行文件路径（环境变量），仅影响测试进程，不改变应用配置；默认运行库期待的版本未安装，将尝试已有 WebKit，若不兼容再获取匹配版本。DEV 截图人工 Review 发现原生按钮未经过 uni-button 样式重置，出现 Safari 默认灰底/边框；已加低优先级原生按钮重置，保持原 .key 字号和确认/再记底色，并在浏览器回归新增计算样式断言。已复审此次样式和断言：仅作用金额键盘内 type=button，不覆盖原生 switch 外观，不改变禁用/焦点语义；复审通过再复验。同步后仍需复审并复验。

## verification
PASS：已执行证据见 testing/commands；截图人工 Review 后默认按钮外观问题已修复并复验。PARTIAL/NOT_RUN：真机系统短触感、VoiceOver 与发布后验收未替代。后端/数据库/Redis/MinIO/真实账务事务因未受影响不适用。

更新范围：仅 H5 需要用户发布完整 app/dist/build/h5 静态产物（HTML、JS、CSS）；后端和微信小程序无需更新。无 SQL/Migration、环境配置或额外远端资源操作，无发布顺序依赖。本轮不执行任何发布。

## rollback
回退本轮按键组件、记账接线、定向回归与必要文档，重建并由用户发布 H5。无数据库或用户数据回滚。

## 同步后复审与验证

精确路径 stash -u 保存全部 12 个任务文件（含截图）；git pull --ff-only 返回 Already up to date，git fetch gitee main 成功。GitHub/Gitee 与本地 c29cce9 双向 log 均无独有提交。stash apply 后 12 文件 SHA-256 全相同，确认恢复后才删除备份。同步未改变代码/依赖，无冲突或未合并文件。

同步后仅复审本轮 H5 接线、组件/探测、两份回归和必要文档：H5 平台隔离、input 原生外观、button 样式重置及焦点、单次事件/禁用和既有保存业务均保持前述语义，无未解决问题。随后执行定向与最终生产浏览器复验；源码哈希未变，已执行的全量回归/类型/双端构建及 DEV/滚动证据仍对应最终代码。

复验结果 PASS：同步后的 9/9 定向回归与最终 Chromium/WebKit 8 场景全部通过，退出 0；本轮 Markdown 本地链接/截图存在、Git 空白检查通过，无冲突标记。DEV 与只读夹具服务均为本轮临时启动，验证后停止，恢复开始时无本轮监听的状态。提交与普通双推完成后由 Git 远端结果核对，不触发 Jenkins 或平台镜像。
