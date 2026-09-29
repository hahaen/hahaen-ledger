# 命令与实际结果

在 `app/` 运行：

| 命令 | 结果 |
| --- | --- |
| `node --test tests/date-picker-scroll.test.mjs`（修复前） | FAIL 0/2；9 月变 7 月，1 日变 3 日。 |
| `node --test tests/date-picker-scroll.test.mjs`（修复后初检） | PASS 2/2。 |
| `node --test tests/date-picker-scroll.test.mjs tests/items.test.mjs` | PASS 9/9。 |
| `pnpm run typecheck` | PASS，退出码 0。 |
| `node --test tests/*.test.mjs` | PASS 87/87；输出包含既有 `onBeforeUnmount` 测试警告。 |
| `pnpm run build:h5` | PASS，`DONE Build complete`；输出包含既有 Sass API 弃用提示。 |
| `pnpm run build:mp-weixin` | PASS，`DONE Build complete`；未导入开发者工具。 |
| `git diff --check` | PASS，退出码 0。 |

Chrome 当前 H5 物品页的只读 DOM：修复前日期字段 `2026-09-29`，滚轮高亮为 2026 年、7 月、29 日；修复后高亮为 2026 年、9 月、29 日，月份 `scrollTop=384`、年份 `scrollTop=1248`。浏览器标签页的点击桥接两次超时；改由本机 Chrome 窗口坐标操作后，实际点选 8 月、27 日，DOM 高亮为 `2026年/8月/27日`，点击确定后表单回写 `2026-08-27`。随后拖动月份列至末端，DOM 高亮保持 12 月、滚动值为 528；拖动年份列至末端，DOM 高亮为 2099 年、滚动值为 4752。两次选择未来日期后点击确定均提示日期须在 2000-01-01 至 2026-09-29 之间，表单仍为 2026-09-29。未提交物品；取消并重新打开后，日期字段和滚轮已恢复 `2026-09-29`。
