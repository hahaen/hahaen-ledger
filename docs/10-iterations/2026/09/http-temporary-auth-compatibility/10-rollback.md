# 回滚

将服务器环境变量 `H5_ALLOW_INSECURE_PASSWORD_OVER_HTTP` 设为 `false` 或删除后重启服务，即刻拒绝 HTTP 兼容密码载荷；随后可回退本次前后端代码和文档。无需数据库回滚。
