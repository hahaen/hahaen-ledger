# Verification

- `PASS`：线上 upload-url 返回 200。
- `PASS`：失败 PUT 已定位为公网 `/minio-api/` 返回 403。
- `PASS`：后端 Endpoint、公网前缀、Bucket、时间和 MinIO 管理连通性已核对。
- `PASS`：失败 PUT 的 SigV4 计算与内网 Host `172.20.0.1:9000` 匹配，说明后端签名材料本身一致。
- `PASS`：仓库 Nginx 示例已补齐 `Connection`、chunked transfer、请求体大小和超时配置。
- `PASS`：正式 Nginx 已应用配置并 reload；`nginx -t` 成功。
- `PASS`：正式域名同类 SigV4 临时 PUT 返回 HTTP 200，MinIO 读回成功，临时对象已清理。
- `PASS`：真实 H5 头像链路在 reload 后取得连续运行证据：`upload-url` 200、预签名 MinIO PUT 200、`complete` 200、`view-url` 200、对象 GET 200。个人资料保存按钮未纳入本次上传验收。
