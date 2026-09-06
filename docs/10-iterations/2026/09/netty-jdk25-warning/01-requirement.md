# 需求

## 现象

Java 25 启动后，Netty 4.1.124.Final 调用 `sun.misc.Unsafe.allocateMemory`，输出 terminally deprecated 警告。应用本身仍能启动，但旧版依赖存在后续 JDK 兼容性和安全修复滞后风险。

## 验收标准

1. Netty 统一升级到当前 4.1 维护线版本。
2. `mvn spring-boot:run` 使用 Java 25 启动时不再出现 Netty `Unsafe` 警告。
3. Redis/Lettuce 依赖解析、单元测试和打包保持成功。
4. 生产 JAR 的推荐启动方式和回滚方式有文档记录。
