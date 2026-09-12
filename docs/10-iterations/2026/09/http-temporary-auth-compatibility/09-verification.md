# 验证

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| HTTP 兼容载荷边界 | PASS | `H5AuthServiceTest` 4/4：仅显式开关开启且 HTTP 请求时接收兼容载荷；默认关闭或 HTTPS 请求均拒绝；既有 RSA 密文注册仍通过。 |
| 前端载荷与类型检查 | PASS | `page-flows.test.mjs` 30/30 验证认证页使用独立兼容载荷、个人中心仍使用 RSA 密文字段；`pnpm run typecheck` 退出码 0。 |
| 后端全量与 H5 构建 | PASS | `mvn test` 43/43；使用临时本地 API 基址的 `pnpm run build:h5` 完成。 |
| 真实公网 HTTP 注册与登录 | NOT_RUN | 待部署时使用显式临时开关验证。 |
| HTTPS RSA 注册与登录 | NOT_RUN | 待具备 HTTPS 环境后验证。 |
