# 测试

## 已执行

| 范围 | 结果 |
| --- | --- |
| 前端 Node 回归 | PASS：`node --test tests/*.test.mjs`，53/53 通过 |
| TypeScript | PASS：`pnpm typecheck`，exit 0 |
| H5 生产构建 | PASS：`pnpm build:h5`，输出 `DONE Build complete.` |
| 微信小程序生产构建 | PASS：`pnpm build:mp-weixin`，输出 `DONE Build complete.` |
| 空白检查 | PASS：`git diff --check`，无差异错误 |
| 小程序产物静态检查 | PASS：`calendar.wxml` 的日期格/数字为 `view`，`app.wxss` 强制保留 `.calendar-cell.selected .day-num` 的 `#49ad9c` 背景 |

## 未执行

- 微信开发者工具导入、真实登录态进入日历、点击日期和截图：BLOCKED / NOT_RUN。
- 真实 API、数据库和账单数据联调：NOT_RUN；本次无后端变更。
