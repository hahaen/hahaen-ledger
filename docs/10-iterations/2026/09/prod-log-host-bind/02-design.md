# 设计

在 `docker-compose.prod.yml` 为 `haji-server` 配置宿主机 `/home/hahaen/log/haji` 到容器 `/home/hahaen/log/haji` 的 bind mount。Jenkins 已用 Compose 部署该文件，因此后续正常部署会自动应用挂载。

应用以容器内路径写日志；挂载后同一文件可从宿主机路径访问。首次启用前，旧容器中同路径的文件会被新挂载遮住，需在部署前另行备份或复制。
