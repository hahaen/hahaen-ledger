# 验证

| 项目 | 状态 | 证据 |
| --- | --- | --- |
| Jenkinsfile、Dockerfile、Compose 文件生成 | PASS | 工作区已生成部署文件，业务源码未改动。 |
| Git whitespace 检查 | PASS | `git diff --check` 无输出。 |
| Jenkinsfile 实际获取与解析 | PARTIAL | Jenkins 已从 Gitee 获取 `server/Jenkinsfile`；首次因未安装 Timestamper 插件而在 `timestamps()` 处解析失败，已移除该选项，待重新提交后验证。 |
| Jenkins SSH 实际构建 | NOT_RUN | 尚未执行到 SSH、Compose 和服务探针阶段。 |
| Docker Compose 生产构建 | NOT_RUN | 尚未在服务器构建镜像。 |
| Nginx 路由示例 | PASS（静态） | 已按最终入口配置更新 `deploy/nginx/haji-api-location.conf.example`：`/jenkins/` 保留前缀代理，`/haji-api/` 去除前缀后转发既有 `/api/...`，其余请求用于 H5 SPA 回退。 |
| Nginx 语法、reload 与真实代理 | NOT_RUN | 本次仅同步仓库示例与说明，未连接服务器执行 `nginx -t`、reload 或 HTTP 探针。 |
| 真实生产服务验收 | BLOCKED | 需要服务器 `/home/hahaen/haji-prod.env` 和部署窗口。 |
