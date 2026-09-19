# 测试

- 8 个远端 MinIO 对象：HEAD 均为 HTTP 200，字节数逐一匹配本地原图。
- 前端回归：`pnpm exec node --test tests/*.test.mjs`，55/55 通过。
- 前端：`pnpm run typecheck` 通过。
- 小程序生产构建：`pnpm run build:mp-weixin` 通过。
- 未执行微信开发者工具重新导入、上传或真机网络加载。
