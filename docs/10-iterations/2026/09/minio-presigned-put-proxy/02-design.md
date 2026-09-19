# Design

后端继续使用内网 `MINIO_ENDPOINT` 生成签名，Nginx `/minio-api/` 去掉公网路径前缀并将上游 Host 保持为 `$proxy_host`，确保 canonical Host/path 与签名一致。补齐 MinIO 官方反向代理所需的空 Connection、非 chunked 请求体、无限制请求体和长超时配置。
