# 验证

| 验收项 | 状态 | 证据/限制 |
| --- | --- | --- |
| 默认首路由为首页 | PASS（自动化回归） | 测试断言 `pages.json` 首项为 `pages/index/index` |
| 启动认证成功不跳首次使用页 | PASS（静态+自动化回归） | `App.vue` 已无 `first-use-complete` 和首次使用页跳转 |
| 首次使用页保留 | PASS（自动化回归） | `pages.json` 仍注册 `pages/first-use/first-use` |
| 微信小程序生产构建 | PASS | uni-app 构建完成 |
| 真实微信开发者工具启动画面 | NOT_RUN | 当前未重新启动微信开发者工具进行人工确认 |
