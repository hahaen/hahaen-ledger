# Testing

- 静态：检查 Nginx 示例包含 Host、Connection、chunked transfer、请求体和超时配置。
- 运行：重新加载正式 Nginx 后，使用已登录 H5 选择头像，确认 upload-url=200、MinIO PUT=200/204、complete=200、view-url=200，并确认对象可回读。
- 真实运行证据取得前，正式上传状态保持 `NOT_RUN` 或 `BLOCKED`，不以构建替代。
