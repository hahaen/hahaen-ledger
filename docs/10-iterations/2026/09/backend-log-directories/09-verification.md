# 验证

| 项目 | 状态 | 证据 |
| --- | --- | --- |
| Profile 配置加载 | PASS | `LoggingProfileConfigTest` 加载 `application-dev.yml`、`application-prod.yml` 并断言两个目录；1/1 通过。 |
| 后端完整测试 | PASS | `server` 目录执行 `mvn test`，41/41 通过，0 failures/errors/skipped。 |
| 真实开发环境文件写入 | NOT_RUN | 待启动依赖服务后验证。 |
| 真实生产环境文件写入 | NOT_RUN | 需要生产主机目录权限与部署窗口。 |
