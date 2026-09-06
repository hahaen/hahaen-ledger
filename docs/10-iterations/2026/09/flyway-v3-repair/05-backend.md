# 后端

- 先复现 Flyway 校验错误。
- 修复后执行 Maven 测试、打包及应用启动验证。
- 不在 Controller 或业务 Service 中绕过 Flyway。

验证结果：Spring Boot `dev` Profile 启动成功，Flyway 校验 3 个迁移通过，数据库当前版本 v3，Tomcat 启动于 8080；Redis PING 和 MinIO Bucket 探测成功。

状态：PASS。
