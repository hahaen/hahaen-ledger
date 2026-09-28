# 命令与结果

- `cd app && pnpm exec node --test tests/*.test.mjs`：PASS，76/76。首次执行时页面夹具未注入新计算器振动模块，导致记账页夹具加载失败；补齐测试依赖后重跑通过。
- `cd app && pnpm run typecheck`：PASS，`vue-tsc --noEmit` exit 0。
- `cd app && pnpm run build:h5`：PASS，exit 0。
- `cd app && pnpm run build:mp-weixin`：PASS，exit 0。
- 构建产物 API 静态核对：PASS。H5 27 个 JS/JSON/WXML/HTML 产物中振动 API 仅出现在记账页 chunk；微信 88 个同类产物中仅 `utils/calculatorFeedback.js` 含 `vibrateShort`。源代码中仅计算器专用模块包含振动 API。
- `git diff --check`：PASS，无空白错误。
- H5 移动浏览器、微信开发者工具/真机实际触摸振动：NOT_RUN。
