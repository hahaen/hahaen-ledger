# Backend

后端代码不变。现有 `MinioStorageService` 继续以内网 Endpoint 签名，并将返回 URL 的公网前缀映射到 `/minio-api`。本轮修复点位于 Nginx 反向代理，不在业务服务。
