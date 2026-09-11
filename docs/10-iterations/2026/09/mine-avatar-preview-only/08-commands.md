# 命令

| 命令 | 结果 |
| --- | --- |
| `app: node --test tests/page-flows.test.mjs` | PASS：26 tests passed，0 failed。 |
| `app: pnpm run typecheck` | PASS：exit 0。 |
| `app: pnpm run build:h5` | FAIL：未配置 `VITE_API_BASE_URL`，构建按项目保护规则拒绝执行。 |
| `app: $env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5` | PASS：构建完成；仅输出既有 Dart Sass legacy JS API 弃用警告。 |
| `app: pnpm run build:mp-weixin` | FAIL：未配置 `VITE_API_BASE_URL`，构建按项目保护规则拒绝执行。 |
| `app: $env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin` | PASS：构建完成；仅输出既有 Dart Sass legacy JS API 弃用警告。 |
| `git diff --check` | PASS：exit 0。 |
