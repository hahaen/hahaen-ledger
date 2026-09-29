# Commands

2026-09-28 在 `app/` 执行：

- `node --test tests/password-crypto.test.mjs`：PASS，2/2。
- `node --test tests/*.test.mjs`：PASS，82/82。
- `pnpm run typecheck`：PASS。
- `pnpm run build:mp-weixin`：PASS；产物 `common/vendor.js` 中 `util.globalScope` 返回 `globalThis`。
- `pnpm run build:h5`：PASS。
- `pnpm run dev:mp-weixin`：PASS，开发产物更新并持续监听。
- `git diff --check`：PASS。

微信开发者工具重新加载：PASS，首页内容可见。控制台在修复前留下的错误记录未作为修复后新错误计入。
