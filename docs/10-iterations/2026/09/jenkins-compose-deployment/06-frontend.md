# 前端

- 新增 `app/Jenkinsfile`，使用 Node 22 容器执行 pnpm 安装、类型检查和 H5 构建；与后端流水线一致，不依赖 Timestamper 插件的 `timestamps()`。
- 构建产物复制到宿主机 Nginx 挂载目录 `/home/hahaen/nginx/html`。
- 新增 `deploy/nginx/haji-api-location.conf.example`，仅作为手工合并到现有 Nginx `server {}` 的示例。
