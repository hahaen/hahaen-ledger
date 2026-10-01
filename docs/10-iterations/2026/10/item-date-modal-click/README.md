# 物品日期弹窗点击修复

日期：2026-10-01。

## requirement

修复微信物品新增、编辑购买日期点击无响应；补验共享物品退役日期入口。保持原有字段、视觉、日期约束及保存流程。

## design

修复前本机微信工具点击新增购买日期，整个 CenterModal 关闭，日期弹窗未显示。三个假设：共享背景 `.self` 在微信误判内部点击；二级弹窗层级遮挡；保存中禁用按钮。工具显示按钮可用、点击后整个编辑器移除，结合共用组件绑定，优先隔离背景点击。使用独立遮罩与内容兄弟节点；触摸禁止仅位于遮罩，H5 内容停止冒泡。

## database / api / backend

无变更；Schema、权限、金额事务、幂等、Redis、MinIO及Java验证不适用。不保存真实物品测试数据。

## frontend

CenterModal 背景与内容事件隔离，保留背景关闭、关闭按钮、closeOnBackdrop 开关与滚动内容。

## testing / commands / verification

修复前本机微信工具：FAIL，新增购买日期点击导致编辑器关闭。真实模板 AST 回归修复前 FAIL（祖先仍绑定 click.self 与 touchmove.stop.prevent），修复后 PASS。

| 已执行命令/动作 | 结果 |
| --- | --- |
| node --test app/tests/item-date-modal.test.mjs | PASS，1/1；先修正测试依赖入口为 vue/compiler-sfc，再确认修复前业务断言失败 |
| node --test app/tests/*.test.mjs | PASS，146/146 |
| pnpm --dir app run typecheck | PASS，退出0 |
| pnpm --dir app run build:h5 | PASS，退出0，既有 Sass legacy-js-api 提示 |
| pnpm --dir app run build:mp-weixin | PASS，退出0，既有 Sass legacy-js-api 提示 |
| 微信生产 CenterModal.wxml 检查 | PASS，根无关闭/touchmove绑定，catchtap/catchtouchmove只在独立遮罩，内容使用bindtouchmove，scroll-view无catchtouchmove |
| node app/tests/visual-server.mjs | PASS，18761只读夹具；不连接真实后端，拒绝写入；已停止本轮服务并关闭本轮标签 |
| git diff --check | PASS |
| git stash push --include-untracked / git pull --ff-only / git stash apply | PASS，origin/main已是最新；恢复9个文件SHA-256一致后删除备份 |
| 同步后 node --test app/tests/*.test.mjs / typecheck | PASS，146/146及类型退出0；远端与客户端源码无变化，双端构建仍适用 |

本机微信工具 DEV 实际操作 PASS：新增购买日期 2026-10-01→2026-09-01；编辑购买日期 2026-09-28→2026-09-27；退役日期 2026-10-01→2026-09-30。每次均打开二级日期弹窗，选项点击、确认后回填一级表单，一级表单保留；随后取消草稿，未调用保存。

H5 最新生产产物只读夹具操作 PASS：新增购买日期→2026-09-01、编辑购买日期2026-03-12→2026-03-11、退役日期→2026-09-01；独立背景点击与关闭按钮取消表单，返回详情仍为原购买日期2026-03-12。Playwright 首次角色选择未匹配 uni-button，改用实际 aria-label；关闭测试中即时count读取早于Vue更新，重新读取DOM确认关闭。无运行异常。

PARTIAL：本机微信开发者工具与H5浏览器覆盖，未获得手机真机触摸证据。NOT_RUN（补充）：真机与正式发布、真实物品保存（本轮仅改弹窗事件，不涉及保存业务）。无必需验证 FAIL/BLOCKED；后台与数据库验证不适用。

## rollback

恢复 CenterModal 和对应定位样式，移除本轮回归。无数据库或数据回滚。
