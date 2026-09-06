# Netty Java 25 兼容性优化

- feature-key：`netty-jdk25-warning`
- 类型：基础设施 / 依赖升级 / JDK 兼容性
- 日期：2026-09-06
- 状态：PASS，依赖、构建、开发启动和打包运行均已验证。
- 目标：消除 Java 25 下 Netty 使用 `sun.misc.Unsafe` 产生的启动警告，并保持 Redis/Lettuce 兼容性。

本迭代不修改数据库、API、业务逻辑和前端。
