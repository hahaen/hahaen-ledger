# 已执行命令

在 `server/`：`mvn test`，BUILD SUCCESS，27 tests、0 failures/errors/skipped。

在 `app/`：`pnpm run typecheck`，exit 0；`node --test tests/page-flows.test.mjs`，18/18 通过；临时设置本机 API 地址后，`pnpm run build:h5` 与 `pnpm run build:mp-weixin` 均完成。两次构建均有既有 Sass legacy-js-api 弃用警告。
