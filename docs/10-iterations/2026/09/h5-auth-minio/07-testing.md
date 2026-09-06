# 测试

- 单元测试：账号规范化、验证码一次性消费、RSA 解密、文件业务类型校验和对象 Key 生成。
- 服务端构建与测试：`mvn test`。
- 前端类型检查与 H5 构建：`pnpm run typecheck`、`pnpm run build:h5`。
- MySQL、Redis、MinIO 未启动时，联调相关项如实记录 BLOCKED，不将构建结果替代联调证据。
