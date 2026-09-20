# Commands

- `cd app; pnpm exec node --test tests/responsive-layout.test.mjs`：执行，9/9 PASS。
- `cd app; pnpm run typecheck`：执行，exit 0，PASS。
- `cd app; pnpm run build:h5`：执行，输出 `DONE Build complete.`，PASS。
- `cd app; pnpm run build:mp-weixin`：执行，输出 `DONE Build complete.`，PASS；用于确认 H5 条件样式未阻断小程序产物。
- `cd app; pnpm exec node --test tests/*.test.mjs`：执行，53 PASS、3 FAIL；3 个失败均为既有测试夹具未提供 `uni.vibrateShort`，错误发生在 `page-flows.test.mjs` 保存流程，与本次 SCSS/布局改动无关，整体回归记为 PARTIAL。
- `git diff --check`：执行，无输出，PASS。
