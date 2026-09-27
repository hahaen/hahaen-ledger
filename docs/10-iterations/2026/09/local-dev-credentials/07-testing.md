# 07｜测试

执行 `mvn test -B -Dstyle.color=never`，Maven 在编译阶段失败：当前唯一可用 JDK 为 17.0.3，项目要求 Java 25，错误为“不支持发行版本 25”。测试用例未运行，状态 BLOCKED（本机工具链）。DEV 服务启动和真实基础设施连接未执行。
