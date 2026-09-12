# 设计

- Jenkins 容器通过 `deploy-ssh-key` SSH 登录宿主机 `172.20.0.1`。
- 宿主机用户 `jenkins-deploy` 属于 `docker` 组，由宿主机执行 Compose。
- 后端 Compose 服务加入 `jenkins_default`，供 Nginx 通过 `haji-server:8080` 访问；Nginx 本身也必须加入该外部网络。
- MySQL、Redis、MinIO 使用宿主机网关及已发布端口，避免依赖默认 `bridge` 网络的容器名解析。
- 前端使用 Node 22 容器构建 H5，复制到 `/home/hahaen/nginx/html`。
- Nginx 公开入口统一为 HTTP 80：`/jenkins/` 代理到 `jenkins:8080` 并保留路径前缀，Jenkins 使用 `--prefix=/jenkins`；`/haji-api/` 代理到 `haji-server:8080/`，由末尾 `/` 去掉公网前缀后访问后端既有 `/api/...` 路径；其他路径用于 H5 静态资源与 SPA 回退。
