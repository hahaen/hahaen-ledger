# 验证

| 范围 | 状态 | 证据 |
| --- | --- | --- |
| MinIO 公开对象完整性 | PASS | 8 个对象 HEAD 为 200，Content-Length 与原图逐一一致 |
| 源码静态图片外置 | PASS（静态） | `src/static` 已无这 8 个图片，页面统一调用 `staticResource` |
| 前端回归 | PASS | `pnpm exec node --test tests/*.test.mjs`，55/55 通过 |
| TypeScript | PASS | `pnpm run typecheck` exit 0 |
| 微信小程序生产构建 | PASS | `pnpm run build:mp-weixin` 完成 |
| 主包构建目录尺寸 | PASS | 270.19 KB，小于 2 MB |
| 微信开发者工具实际上传 | NOT_RUN | 需用户重新导入本轮产物并执行上传 |
| 真机图片加载 | NOT_RUN | 未在真实微信网络环境验证 |
