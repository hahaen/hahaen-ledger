# commands

- `node --test tests/page-flows.test.mjs tests/entry.test.mjs`：exit 0，15/15 tests passed。
- `pnpm run typecheck`：exit 0。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`：exit 0，H5 production build complete。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin`：exit 0，微信小程序 production build complete。
