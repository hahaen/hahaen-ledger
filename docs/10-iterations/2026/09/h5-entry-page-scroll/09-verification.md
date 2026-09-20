# Verification

| 验证面 | 状态 | 证据/限制 |
| --- | --- | --- |
| H5 布局静态回归 | PASS | `responsive-layout.test.mjs` 9/9；覆盖 H5 `.entry-page` 视口高度、`.entry-content` `height:0`、flex 和纵向 overflow 规则。 |
| TypeScript | PASS | `pnpm run typecheck` exit 0。 |
| H5 生产构建 | PASS | `pnpm run build:h5` 输出 `DONE Build complete.`。 |
| 微信小程序生产构建 | PASS（产物） | `pnpm run build:mp-weixin` 输出 `DONE Build complete.`；真实开发者工具画面仍未验证。 |
| 全量前端 Node 回归 | PARTIAL | 53 PASS、3 FAIL；失败原因为既有 `uni.vibrateShort is not a function` 测试夹具错误，不是本次改动引入。 |
| H5 390×844 只读运行时布局 | PARTIAL | `.entry-content` 实测 `clientHeight=790`、`scrollHeight=867`、`overflow-y:auto`，数字键盘保持 `position:fixed`；夹具接口未提供真实账务数据，触摸滑动未完成真实 iOS Safari 验收。 |
| 真实 iOS Safari 登录态新增/编辑记账 | NOT_RUN | 当前没有真实登录态与 iOS Safari 真机证据。 |
| 微信开发者工具人工验收 | NOT_RUN | 未导入产物并操作真实小程序页面。 |
