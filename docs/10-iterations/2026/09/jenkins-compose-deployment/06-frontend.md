# 前端

- 新增 `app/Jenkinsfile`，使用 Node 22 容器执行 pnpm 安装、类型检查和 H5 构建；与后端流水线一致，不依赖 Timestamper 插件的 `timestamps()`。
- 构建产物复制到宿主机 Nginx 挂载目录 `/home/hahaen/nginx/html`。
- `deploy/nginx/haji-api-location.conf.example` 已更新为当前完整虚拟主机示例：其 `map` 位于 `http {}` 上下文，Jenkins、`/haji-api/` 和 H5 SPA 路由位于同一个 `server {}`。H5 生产构建应将 `VITE_API_BASE_URL` 配为站点源站加 `/haji-api`。
