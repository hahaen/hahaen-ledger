# 验证

| 项目 | 状态 | 证据 |
| --- | --- | --- |
| Jenkinsfile、Dockerfile、Compose 文件生成 | PASS | 工作区已生成部署文件，业务源码未改动。 |
| Git whitespace 检查 | PASS | `git diff --check` 无输出。 |
| Jenkinsfile 实际获取与解析 | PARTIAL | Jenkins 已从 Gitee 获取 `server/Jenkinsfile`；首次因未安装 Timestamper 插件而在 `timestamps()` 处解析失败，已移除该选项，待重新提交后验证。 |
| Jenkins SSH 实际构建 | NOT_RUN | 尚未执行到 SSH、Compose 和服务探针阶段。 |
| Docker Compose 生产构建 | NOT_RUN | 尚未在服务器构建镜像。 |
| Nginx API 代理 | BLOCKED | 需要将示例 location 合并到现有 Nginx 配置并 reload。 |
| 真实生产服务验收 | BLOCKED | 需要服务器 `/home/hahaen/haji-prod.env` 和部署窗口。 |
