# 后端

- 修改 `server/pom.xml`，将 Netty 统一版本提升至 `4.1.137.Final`。
- 配置 `spring-boot-maven-plugin` 开发启动参数 `-Dio.netty.noUnsafe=true`。
- 在 `LedgerApplication` 主入口设置 `io.netty.noUnsafe=true`，覆盖 IDE 直接启动场景。
- 未修改 Java 业务代码、Redis Key、配置密钥或异常处理。
