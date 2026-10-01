# 记账触摸交互修复

日期：2026-10-01。

## requirement

修复微信记账及共用日期/时间弹窗不能选择、手机 H5 新增/编辑记账主体不能上滑、两端数字键盘缺少振动。只修改客户端交互，保持原有视觉、业务和全局无振动规则。

## design

- H5 `disableScroll` 会注册 document touchmove preventDefault；主体只 stop 冒泡，不阻止原生滚动。
- 微信日期滚轮祖先的 catchtouchmove 会吞掉滚动；背景阻止触摸的层与内容改为兄弟节点。仅 H5 内容 stop 冒泡至页面禁滚监听。
- 微信一级日期时间弹窗的 `.self` 在内部点击后误关一级弹窗，二级确认后无法继续回填。一级弹窗也独立背景层，选项/确认停止点击冒泡。
- 原金额键盘捕获 touchstart 并访问 Element/closest，小程序没有 DOM；H5 首次 touchstart 也可能未获得用户激活。改为按键实际点击触发一次反馈，再执行既有计算/保存。H5 15ms；微信 light 失败后默认短振动。

## database / api / backend

无修改；Schema、Flyway、权限、金额事务、幂等、Redis、MinIO、Java 构建不适用。没有提交真实账单或修改用户账户数据。

## frontend

修改 EntryDateTimePicker、MonthPicker、entry 页面、calculatorFeedback 和背景层定位。保留 2000–2099 年显示范围、业务日期上下限、闰月/日归一化、固定键盘及保存禁用状态。

## testing

- 修复前 `node --test app/tests/entry-touch.test.mjs` 3 项失败，键盘报 Element is not defined；修复后 5 项通过，包括逐键反馈/保存/禁用、平台振动及旧微信回退。
- 最终全量 145/145 PASS，类型检查与双端生产构建 PASS。
- H5 本机 DEV 新增 375×667，同一触摸上滑 scrollTop 0→136.5px。
- H5 最终生产夹具新增 375×667 滚动 136.5px，667×375 横屏滚动 258.5px；普通支出编辑滚动 127.5px；还款编辑已验证 191.5px。375×667 键盘顶部保持 y=395。
- H5 日期触摸选中 2026-12-15 并确认回填；还款编辑时间 10:00→10:04 并二级/一级确认回填。只读夹具，不连接真实后端，不作为真实账单保存证据。
- H5 实际页面按键 1/退格，临时振动探针记录 [15,15]，随后恢复原方法并删除探针；所有视口/触摸仿真已撤销。
- 本机微信工具：月份点击 10→11，分钟列滚动 11→16，二级确认回到一级弹窗，一级确认记账字段为 2026-11-01 11:16；键盘 1+2 显示 3，未保存账单。
- 模拟器原生鼠标 drag 未产生可靠触摸滚动信号，未用其声明微信真机触摸 PASS。微信滚轮鼠标滚动、选项点击及回填和无 catchtouchmove 编译检查为已执行证据。

## commands

| 已执行命令/动作 | 结果 |
| --- | --- |
| node --test app/tests/entry-touch.test.mjs（修复前） | FAIL，3/3；Element is not defined 和触摸隔离断言失败 |
| node --test app/tests/entry-touch.test.mjs（修复后） | PASS，5/5 |
| node --test app/tests/*.test.mjs（最终） | PASS，145/145 |
| pnpm --dir app run typecheck | PASS，退出0 |
| pnpm --dir app run build:h5 | PASS，退出0；既有 Sass legacy-js-api 提示 |
| pnpm --dir app run build:mp-weixin | PASS，退出0；既有 Sass legacy-js-api 提示 |
| node app/tests/visual-server.mjs | PASS，本机只读视觉夹具启动，用于实际 H5 生产产物 |
| adb devices（本机 SDK 完整路径） | 已执行，设备列表为空 |
| git diff --check | PASS |
| git stash push --include-untracked / git pull --ff-only / git stash apply | PASS，origin/main 已是最新；恢复后19个文件 SHA-256 一致，再删除备份 |
| 同步后 node --test app/tests/*.test.mjs / typecheck | PASS，145/145、类型退出0；远端及源文件未变化，已有双端构建仍适用 |
| 微信生产 WXML/JS 核对 | PASS，背景独立、scroll-view 无 catchtouchmove、键盘无 Element 依赖 |

## verification

PASS：上述已执行的回归、类型、构建、H5 触摸滚动/选择、微信选择/滚动/回填、H5 振动 API 逐键调用。

PARTIAL：跨端实际硬件振动；API/测试和模拟器结果不能证明手机震感。共享组件的所有业务调用页未逐页操作。

BLOCKED（补充验收）：本机没有连接可用的 Android 真机；手机平台与浏览器尚未确认，微信真机手势及震感、iOS Safari 实测不可执行。不作为本轮自动提交的必需验证门禁，客户端必需验证以上述真实浏览器/微信工具操作与构建回归为准。

NOT_RUN（补充）：正式环境部署、真实账单保存与全部调用页真机验收。无遗留代码测试 FAIL。

H5 振动受浏览器/系统支持约束，未支持不能通过客户端代码保证振动，参考 [MDN Navigator.vibrate](https://developer.mozilla.org/en-US/docs/Web/API/Navigator/vibrate)。

证据：[H5 编辑主体/日期回填](../../../../../app/tests/evidence/entry-h5-touch-20261001.png)、[微信日期选中](../../../../../app/tests/evidence/entry-wechat-date-20261001.png)、[微信最终回填](../../../../../app/tests/evidence/entry-wechat-selected-20261001.png)。

## rollback

恢复本次组件背景事件、记账主体 touchmove 和按键反馈绑定，删除本轮新增测试及样式。无数据库或数据回滚。
