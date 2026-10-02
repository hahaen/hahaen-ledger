# 新增与编辑记账真机滚动修复

日期：2026-10-02。

## requirement

用户报告开发 H5/微信可滑动，手机 Safari、Chrome 和微信不可滑动。新增与编辑共用页面；主体中的账户、日期、备注须可到达，保留固定导航、金额键盘与原有账务逻辑。

## design

- 已验证代码/产物问题：普通 `view` 只配置 CSS overflow，页面配置 disableScroll；微信主体编译为 `<view class="entry-content" catchtouchmove>`，不是原生滚动区。H5 当前依赖 stopPropagation 避开框架的 document touchmove preventDefault；本机 Chrome 仿真能滚动，不能将其当作手机故障已复现。
- 排查候选：普通 view 跨端滚动能力、全局触摸取消、flex 高度/键盘遮挡。微信产物核对定位前两点；实际浏览器几何与触摸验证排查第三点。
- 使用 `scroll-view scroll-y`，明确 flex 剩余高度（height:0 / min-height:0），内层承担左右和键盘留白。移除页面 disableScroll 与主体 touchmove 事件，外层 overflow:hidden 继续限制页面外溢。
- 账户选择弹窗存在同类祖先 catchtouchmove，遮罩与内容分离，内容定位于遮罩上方，滚动区不绑定 touchmove。
- 官方依据：[uni-app scroll-view](https://uniapp.dcloud.io/component/scroll-view.html) 要求纵向滚动有明确高度。UI 技能搜索未命中滚动专项，采用该项目布局与官方组件要求，不引入视觉重设计。

## database

无 Schema、Entity、Flyway 或数据变更，不适用。

## api

无 API、DTO、权限与认证变更，不适用。

## backend

无后端变更，金额事务、幂等、Redis、MinIO 和 Java 构建不适用；未执行真实账务写入。

## frontend

修改 entry 模板、pages.json 和主体样式。新增/支出编辑/转账编辑/还款编辑共用同一容器；键盘、导航、保存逻辑保持原规则。主体内层接管横屏自适应键盘留白。更新旧静态断言，增加 Vue 编译 AST 检查与实际 H5 生产触摸回归脚本。

## testing

- AST 回归修复前 2/2 FAIL（普通 view、账户弹窗祖先 touchmove），修复后通过；另补页面 disableScroll 禁用监听检查。该测试锁定结构，不冒充微信真机手势验证。
- 最终全量 149/149 PASS；类型检查及双端生产构建 PASS，仅既有 Sass legacy-js-api 提示。
- 实际 H5 生产只读夹具：375×667、320×568、390×844、667×375，每个尺寸覆盖新增、支出编辑（id=2）、转账编辑（id=1）、还款编辑（id=6）；使用 CDP 触摸事件上滑/回滑，断言 scrollTop、备注完整可见、键盘位置不变，备注打开/取消。内容未溢出的高屏新增/支出编辑允许 scrollTop=0，此时字段已全部在键盘上方。
- 初版浏览器脚本遇到重复 .uni-scroll-view 定位、无溢出高屏误报、横屏滑动次数不足；分别修正为内部实际滚动节点、按实际 scrollRange 判定及更多连续手势。不是以程序设置 scrollTop 替代手势。
- 微信工具最终生产目录：新增页滚轮操作使主体上移（截图金额面板约 y394→374），键盘顶部保持 y1028；账户选择切换并按现有交互回填；返回首页取消草稿。工具无错误；不把鼠标滚轮记为手机手指验证。

## commands

| 已执行 | 结果 |
| --- | --- |
| node --test app/tests/entry-native-scroll.test.mjs（修复前） | FAIL，2/2；另一次最初测试因 compiler-sfc 包导入不正确退出，改用仓库 vue/compiler-sfc 后形成上述失败证据 |
| node --test app/tests/*.test.mjs（最终） | PASS，149/149 |
| pnpm --dir app run typecheck | PASS，退出0 |
| pnpm --dir app run build:h5 | PASS，退出0 |
| pnpm --dir app run build:mp-weixin | PASS，退出0 |
| pnpm --dir app run dev:mp-weixin | PASS，开发产物更新；验证结束停止本轮 watcher |
| node app/tests/visual-server.mjs | 只读生产产物夹具；禁止真实后端连接与写入 |
| node app/tests/entry-scroll-browser.cjs | 使用 PLAYWRIGHT_MODULE_PATH 指向本机运行库，CHROME_EXECUTABLE_PATH 指向已安装 Chrome；PASS，16/16；见 h5-touch-results.json |
| 微信生产 WXML/JSON 核对 | PASS，主体 scroll-view scroll-y，无 catchtouchmove，页面 JSON 无 disableScroll |
| adb devices（SDK 完整路径） | 已执行，设备列表为空 |
| git diff --check | PASS |
| 同步后 node --test app/tests/*.test.mjs / typecheck | PASS，149/149、类型退出0；代码与依赖未变化，双端构建结果仍适用 |
| 提交轮 node --test app/tests/*.test.mjs / typecheck / 双端构建 | PASS，149/149、类型退出0、H5/微信构建退出0 |
| git stash push --include-untracked / git pull --ff-only / git stash apply | PASS，origin/main 已是最新；14个文件 SHA-256 一致后删除备份 |

重跑浏览器回归：先构建 H5 并启动 visual-server，再通过上述两个环境变量选择本机 Playwright 与 Chrome，运行 entry-scroll-browser.cjs；它仅访问 127.0.0.1:18761 只读夹具。其结果不能代替 Safari 或手机 Chrome。

## verification

PASS：149项回归、类型、双端构建、微信生产事件结构、工具主体滚轮/账户选择与取消、上述 H5 仿真运行验证。

PARTIAL：代码修复完成，用户报告的三种真机环境尚未验收；H5 本机旧版仿真也能滚动，所以无法从仿真证据声称已复现/消除 Safari 的全部触发条件。

BLOCKED：没有可操作的手机设备或真机调试会话，手机 Safari/Chrome 与微信手指上滑/惯性/回滑结果未知。本轮将真机验证作为必需验收，不降为补充项。

NOT_RUN：部署/发布、新旧真机对照与真实账单保存。前一轮依据 AGENTS.md 保留未提交；用户随后明确要求“提交代码”，本轮按当次指令同步 upstream、复验、提交并推送，真机验收仍为 BLOCKED。没有修改用户账务数据。

H5 运行数据：[16组触摸回归结果](h5-touch-results.json)。

手机验收步骤：使用本轮最新 H5/小程序产物，分别在 Safari、Chrome、微信打开新增和编辑；从金额区、账户行、日期行上滑到备注，再下滑回顶部，检查键盘与标题固定；小屏和转账/还款增加第二账户行后重复；打开/关闭弹窗后重复。不得以旧缓存产物判断本次修复结果。

## rollback

恢复本次 entry 模板、pages.json、样式、测试及规范文档。无数据库或数据回滚。
