# 08 命令

- `pnpm exec node --test tests/*.test.mjs`（`app/`）：exit 0，55/55。
- `pnpm run typecheck`（`app/`）：exit 0。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5`（`app/`）：exit 0。
- `$env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin`（`app/`）：exit 0。
- `rg -o 'getMenuButtonBoundingClientRect|screen-nav.menu-aligned|welcome-brand.menu-aligned' dist/build/mp-weixin/utils/nativeNavigation.js dist/build/mp-weixin/components/NativeNavigation.wxss`（`app/`）：exit 0；命中胶囊 API 和横向导航规则。
- `app.wxss` 产物静态核对：确认页面基础安全区规则之后追加 `.detail-page,.entry-page` max 规则，帮助页覆盖为 `top:0`。
- `git diff --check`（从 `app/` 执行）：exit 0。
