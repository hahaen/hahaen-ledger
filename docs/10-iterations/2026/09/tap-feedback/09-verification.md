# 验证

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 共享按压样式 | PASS | `prototype.scss` 已覆盖按钮和明确可点击控件，新增静态回归通过。 |
| H5 轻触振动逻辑 | PASS（代码/产物） | App 挂载捕获 `pointerdown`，产物包含 `navigator.vibrate(8)`；浏览器是否支持由设备决定。 |
| 微信轻振动逻辑 | PASS（代码/产物） | 各页面 WXML 根节点包含 `capture-bind:touchstart`，构建 JS 包含 `vibrateShort`；真实小程序点击尚未操作。 |
| 前端回归 | PASS | Node 60/60。 |
| TypeScript | PASS | `pnpm run typecheck`。 |
| H5 生产构建 | PASS | `pnpm run build:h5`。 |
| 微信小程序生产构建 | PASS | `pnpm run build:mp-weixin`。 |
| H5 移动浏览器真实振动 | NOT_RUN | 未在真实移动浏览器登录并点击页面。 |
| 微信开发者工具/真机真实振动 | NOT_RUN | 未导入产物进行真实点击和振动验收。 |
