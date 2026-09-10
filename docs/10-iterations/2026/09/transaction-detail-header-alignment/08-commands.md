# commands

- `pnpm run typecheck`：exit 0。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`：exit 0。
- `node tests/visual-server.mjs`：启动只读视觉夹具；在 `http://127.0.0.1:18761/#/pages/detail/detail?id=2` 核对账单详情页头，并与 `/pages/entry/entry` 对照。
