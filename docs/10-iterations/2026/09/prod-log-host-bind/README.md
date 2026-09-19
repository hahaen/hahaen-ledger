# `prod-log-host-bind`｜生产后端日志挂载到宿主机

生产 Profile 将 Spring Boot 文件日志写入容器内 `/home/hahaen/log/haji`。现通过 Docker Compose bind mount 将同一路径映射到宿主机，使日志可从服务器文件系统查看，并在容器重建后保留。

本次只修改生产 Compose 和运维文档，不触发 Jenkins、不连接服务器、不改业务代码或日志格式。现有容器内日志在首次启用挂载时会被挂载目录遮住；正式部署前应按 `10-rollback.md` 的说明先备份。

完整实施与验收证据按 01–10 文件记录。实际生产挂载仍须在下一次 Jenkins 部署后检查宿主机目录。
