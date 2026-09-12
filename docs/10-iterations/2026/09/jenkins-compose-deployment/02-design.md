# 设计

- Jenkins 容器通过 `deploy-ssh-key` SSH 登录宿主机 `172.20.0.1`。
- 宿主机用户 `jenkins-deploy` 属于 `docker` 组，由宿主机执行 Compose。
- 后端 Compose 服务加入 `jenkins_default`，供 Nginx 通过服务名访问。
- MySQL、Redis、MinIO 使用宿主机网关及已发布端口，避免依赖默认 `bridge` 网络的容器名解析。
- 前端使用 Node 22 容器构建 H5，复制到 `/home/hahaen/nginx/html`。
