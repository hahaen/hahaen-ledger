# 回滚

1. 将 `server/pom.xml` 的 `netty.version` 恢复为原有 Spring Boot 管理版本（当前基线为 `4.1.124.Final`）。
2. 移除 `spring-boot-maven-plugin` 的 `jvmArguments` 配置。
3. 恢复 README 中新增的生产启动参数说明。
4. 重新执行 `mvn clean package`，确认回滚后的依赖和构建状态。
