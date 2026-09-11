# 验证

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 退出确认视觉结构 | PASS | `mine.vue` 复用账户/账单删除确认使用的自定义圆角面板、遮罩、提示条和双按钮样式。 |
| 退出登录行为 | PASS | 确认后继续调用 `ledger.logout()`；完成后停留当前页并清理页面资料状态。 |
| 重复提交保护 | PASS | `page-flows.test.mjs` 用例验证退出中第二次确认不再调用注销。 |
| 前端回归 | PASS | `node --test tests/page-flows.test.mjs`，19/19 passed。 |
| 类型检查 | PASS | `pnpm run typecheck`，exit 0。 |
| H5 生产构建 | PASS | 临时注入本地 API 地址后构建完成。 |
| 微信小程序生产构建 | PASS | 临时注入本地 API 地址后构建完成。 |
| 只读运行时视觉验收 | BLOCKED | 内置浏览器访问本机视觉夹具返回 `ERR_BLOCKED_BY_CLIENT`，没有可复核截图。 |
| 真实登录态注销验收 | NOT_RUN | 本次未使用真实用户会话发起注销。 |
