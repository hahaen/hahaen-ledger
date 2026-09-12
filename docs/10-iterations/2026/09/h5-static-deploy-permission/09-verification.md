# 验证

| 项目 | 状态 | 证据 |
| --- | --- | --- |
| 故障定位 | PASS | Jenkins 日志显示 H5 构建完成，随后 `install -d` 对 Nginx 静态目录报 `Operation not permitted`。 |
| 流水线预检修复 | PASS（Jenkins 日志） | Jenkins 已越过目录预检并执行复制命令。 |
| 静态复制权限修复 | PARTIAL | 已定位 `cp -a` 尝试保留 root 所有目标目录时间戳而失败，改为 `cp -R`；尚未由 Jenkins 拉取并执行。 |
| 静态文件发布与 Nginx 重载 | NOT_RUN | 需要重新构建后验证。 |
| 公网 H5 与 `/haji-api/` | NOT_RUN | 需要重新构建后以真实浏览器和 API 请求验证。 |
