# commands

- `cd app; pnpm run typecheck`：PASS，exit 0。
- `cd app; $env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`：PASS，`DONE Build complete.`。
- `cd app; $env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin`：PASS，构建完成。
- `node app/tests/visual-server.mjs` + H5 只读视觉夹具：PASS，打开 `/pages/detail/detail?id=2` 的删除弹层并点击取消；未执行写入。
