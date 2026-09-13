# 测试

计划在 `app/` 目录执行：

- `node --test tests/auth-guard.test.mjs`：覆盖 H5 路由识别、无会话拦截和已认证默认入口分流。
- `pnpm run typecheck`：TypeScript 类型检查。
- 临时注入本地 API 地址后执行 `pnpm run build:h5`：验证 H5 生产构建。
- `git diff --check`：验证补丁无空白错误。

真实浏览器关闭并重新打开需要可用 H5 后端和有效会话，不能由静态测试替代。
