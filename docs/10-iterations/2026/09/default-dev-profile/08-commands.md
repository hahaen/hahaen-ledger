# 命令证据

本文件只记录本次迭代实际执行的无敏感信息检查命令及结果；真实凭证不写入文档。

- PowerShell 静态检查 `server/src/main/resources/application.yml`、两个 DEV 配置文件：通过，检测到默认 `dev` Profile 且文件存在。
- `server/mvn test`：通过，2 tests，0 failures/errors。
- `server/mvn clean package`：通过，BUILD SUCCESS，包含 2 个测试且 0 failures/errors。
- 第一次静态检查因在 `server` 工作目录下重复拼接路径而失败，不作为验证证据；未产生文件变更。
