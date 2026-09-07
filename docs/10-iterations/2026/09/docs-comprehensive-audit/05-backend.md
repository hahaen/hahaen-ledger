# 后端核对

## 已确认

- Controller、Service、Mapper、Entity 分层与当前目录一致；账户、账单、退款写操作使用事务边界，退款和账户锁按业务规则处理。
- `CurrentUser` 从 Sa-Token 获取用户；异常由 `GlobalExceptionHandler` 转为统一响应；日志格式包含 Trace ID。
- H5 验证码为 Redis 一次性消费并带 TTL；文件访问经 `AppFileService` 和 `MinioStorageService`。
- 当前 `mvn test` 实际运行 18 个测试并通过。

## 风险/边界

- 真实 MySQL/Redis/MinIO 未在本次环境执行；单元测试不能替代 Mapper、Flyway、隔离级别和并发事务验证。
- 生产配置对 H5 RSA 私钥保留生成兜底，部署时必须显式注入固定私钥。
- 当前没有微信登录后端 Controller，前端调用旧路径导致小程序认证不闭环。

