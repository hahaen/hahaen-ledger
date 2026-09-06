# 命令证据

本文件只记录本次迭代实际执行的无敏感信息命令和结果；未记录连接地址、密码或令牌。

- `server/mvn help:evaluate '-Dexpression=netty.version' '-DforceStdout' -q`：通过，解析值为 `4.1.137.Final`。
- `server/mvn dependency:tree '-Dincludes=io.netty:*' -Dscope=runtime`：通过，Netty 各模块均为 `4.1.137.Final`。
- `server/mvn clean package`：通过，2 tests、0 failures、0 errors，`BUILD SUCCESS`。
- `server/mvn spring-boot:run '-Dspring-boot.run.arguments=--server.port=0'`：通过，Java 25、dev Profile、Flyway、Redis PING、MinIO Bucket 均成功；应用启动日志未出现 Netty `Unsafe` 警告。
- `server/java '-Dio.netty.noUnsafe=true' -jar target/hahaen-ledger-server-1.0.0.jar --server.port=0`：通过，打包 JAR 启动成功，应用启动日志未出现 Netty `Unsafe` 警告。
- `server/java -jar target/hahaen-ledger-server-1.0.0.jar --server.port=0`：通过，未显式传入 JVM 参数时仍成功启动，证明应用主入口兜底配置生效，应用日志未出现 Netty `Unsafe` 警告。

补充：本机 Maven 3.6.3 在 Maven 进程自身启动时仍输出 Jansi/Guava 的 JDK 兼容性警告；这不是应用运行时 Netty 警告，升级 Maven 3.9+ 可另行处理。
