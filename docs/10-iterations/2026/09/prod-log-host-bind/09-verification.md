# 验证

| 项目 | 状态 | 证据/限制 |
| --- | --- | --- |
| Compose bind mount 配置 | PASS（静态） | Compose 将宿主机与容器内 `/home/hahaen/log/haji` 映射到同一路径。 |
| Docker Compose 配置解析 | NOT_RUN | 本地是否安装 Compose CLI 待确认。 |
| `git diff --check` | PASS | exit 0。 |
| 宿主机日志文件实际写入 | NOT_RUN | 未部署到服务器。 |
| 容器重建后的日志保留 | NOT_RUN | 需生产运行验收。 |
