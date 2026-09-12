# 验证

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 认证页协议提交拦截 | PASS | `page-flows.test.mjs` 28/28；新增回归确认默认未勾选、计算提交条件与 `submit()` 拦截、两条协议链接。 |
| 未登录协议访问 | PASS | 新增回归确认两条路径已注册，且 `isH5AuthPath()` 对两条协议路径返回 `true`。 |
| 协议内容与当前实现边界 | PASS | 静态核对认证、验证码、登录审计、头像服务和数据库结构；回归确认协议包含账号认证、安全运行信息及不出售个人信息的声明。 |
| TypeScript 类型检查 | PASS | `pnpm run typecheck` 退出码 0。 |
| H5 生产构建 | PASS | 设定本地 API 基址后 `pnpm run build:h5` 完成。 |
| 微信小程序生产构建 | PASS | 继同一环境变量后 `pnpm run build:mp-weixin` 完成。 |
| 真实浏览器与微信开发者工具视觉验收 | NOT_RUN | 本次未执行。 |
