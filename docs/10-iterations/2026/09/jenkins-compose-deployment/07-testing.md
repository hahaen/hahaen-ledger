# 测试

| 范围 | 计划 | 当前状态 |
| --- | --- | --- |
| 文件格式 | `git diff --check` | 已执行 |
| Docker 镜像构建 | 宿主机执行 Compose 构建 | NOT_RUN |
| Jenkins 后端流水线 | 从 Gitee 获取 `server/Jenkinsfile`；移除未安装的 Timestamper 插件选项后重新构建 | PARTIAL |
| Jenkins 前端流水线 | Gitee 提交后立即构建 | NOT_RUN |
| 生产服务健康检查 | 依赖生产环境变量和 Nginx 合并 | BLOCKED |
