# 需求

生产应用通过 Jenkins 和 Docker Compose 部署。`logging.file.path` 指定 `/home/hahaen/log/haji`，但容器编排未将该目录挂到宿主机，容器重建后文件日志无法从宿主机稳定读取。

将容器内日志目录绑定到宿主机同路径；保留 Jenkins 现有构建/部署流程、控制台日志、日志配置和业务行为。
