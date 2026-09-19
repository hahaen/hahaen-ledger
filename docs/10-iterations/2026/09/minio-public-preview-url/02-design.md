# 设计

- `MINIO_ENDPOINT` 仍用于 MinIO SDK 的对象访问和签名，避免让后端依赖公网回环。
- 新增 `MINIO_PUBLIC_URL_PREFIX`，仅在签名 URL 返回客户端前替换 origin/path 前缀；不改签名查询参数或对象路径。
- 生产必须由部署环境设置绝对 HTTP(S) `MINIO_PUBLIC_URL_PREFIX`，不包含查询或片段；开发默认空值并沿用原 Endpoint。
- 代理需把 `/minio-api/` 映射为 MinIO 根路径，并把 MinIO 收到的 Host 保留为 Endpoint 的 Host；修改部署代理时必须保持路径及 Host 与签名输入一致。
