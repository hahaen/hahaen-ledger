# 验证

当前状态：PASS（本迭代范围）。

- 依赖升级：PASS。Maven 依赖树显示 Netty 各模块统一为 `4.1.137.Final`。
- 单元测试与打包：PASS。2 tests、0 failures、0 errors，`BUILD SUCCESS`。
- Java 25 开发启动及 Netty 警告复核：PASS。`mvn spring-boot:run` 成功启动，Redis PING 和 MinIO Bucket 探测成功，未出现 Netty `Unsafe` 警告。
- 打包 JAR 运行复核：PASS。显式传入 `-Dio.netty.noUnsafe=true` 后成功启动，未出现 Netty `Unsafe` 警告。
- 应用主入口兜底：PASS。未显式传入 JVM 参数直接运行打包 JAR，成功启动且应用日志未出现 Netty `Unsafe` 警告，可覆盖 IDE 直接启动场景。
- Maven 工具链余留警告：PARTIAL。本机 Maven 3.6.3 的 Jansi/Guava 警告仍存在，不属于应用 Netty 警告；升级 Maven 3.9+ 尚未在本次迭代执行。
