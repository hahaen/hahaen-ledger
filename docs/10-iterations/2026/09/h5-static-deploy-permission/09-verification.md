# 验证

| 项目 | 状态 | 证据 |
| --- | --- | --- |
| 故障定位 | PASS | Jenkins 日志显示 H5 构建完成，随后 `install -d` 对 Nginx 静态目录报 `Operation not permitted`。 |
| 流水线预检修复 | PARTIAL | 代码已改为不修改既有目录权限的读写检查；尚未由 Jenkins 拉取并执行。 |
| 静态文件发布与 Nginx 重载 | NOT_RUN | 需要重新构建后验证。 |
| 公网 H5 与 `/haji-api/` | NOT_RUN | 需要重新构建后以真实浏览器和 API 请求验证。 |
