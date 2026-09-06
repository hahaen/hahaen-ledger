# 验证

配置静态检查：PASS。默认 Profile 为 `dev`，占位符默认值为 `dev`，可通过 `SPRING_PROFILES_ACTIVE` 覆盖；`application-dev.yml` 和 `application-dev.example.yml` 均存在。

后端测试：PASS。`server/mvn test` 实际结果为 2 tests、0 failures、0 errors。

真实 DEV 启动及基础设施连接：BLOCKED，需可用的 MySQL、Redis、MinIO 环境。
