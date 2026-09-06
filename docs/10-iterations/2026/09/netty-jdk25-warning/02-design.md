# 设计

## 方案

- 在 Maven properties 中覆盖 Spring Boot 管理的 `netty.version` 为 `4.1.137.Final`，确保 Netty 各模块版本一致。
- 保留 Netty 4.1，不直接切换 Netty 4.2，避免超出当前 Spring Boot/Lettuce 组合的兼容范围。
- 在应用主入口最早阶段设置 `io.netty.noUnsafe=true`，覆盖 IDEA 直接运行主类、Maven 和 JAR 启动方式；Maven/JAR 启动参数仍保留，确保 Netty 在应用入口前被其他组件加载时也有保护。
- 如果对极限 Netty 性能有要求，可单独压测后选择保留 Unsafe 并使用 JDK 的 allow 参数。

## 风险

禁用 Unsafe 可能带来轻微的 Netty 内存访问性能下降，但本项目是单用户记账应用，兼容性和日志稳定性优先；后续如有高并发场景，应以压测结果决定是否调整。
