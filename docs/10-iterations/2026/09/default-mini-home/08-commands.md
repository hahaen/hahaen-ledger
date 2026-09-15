# 命令与结果

| 范围 | 命令 | 结果 |
| --- | --- | --- |
| 前端流程回归 | `pnpm exec node --test tests/page-flows.test.mjs` | PASS，33/33 |
| 类型检查 | `pnpm run typecheck` | PASS |
| 微信小程序构建 | `pnpm run build:mp-weixin` | PASS，生成 `dist/build/mp-weixin` |
| 真实微信工具重新打开 | 微信开发者工具人工操作 | NOT_RUN |
