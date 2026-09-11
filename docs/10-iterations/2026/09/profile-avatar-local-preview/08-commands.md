# 命令

| 命令 | 结果 |
| --- | --- |
| `app: node --test tests/page-flows.test.mjs` | PASS：26 tests passed，0 failed；测试运行时输出既有 Vue 生命周期警告。 |
| `app: pnpm run typecheck` | PASS：exit 0。 |
| `app: $env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:h5` | PASS：构建完成；仅输出既有 Dart Sass legacy JS API 弃用警告与 uni-app 更新提示。 |
| `app: $env:VITE_API_BASE_URL='http://127.0.0.1:8080'; pnpm run build:mp-weixin` | PASS：构建完成；仅输出既有 Dart Sass legacy JS API 弃用警告与 uni-app 更新提示。 |
| `git diff --check` | PASS：exit 0；工作区含用户既有未提交修改，未改动或重置其内容。 |
