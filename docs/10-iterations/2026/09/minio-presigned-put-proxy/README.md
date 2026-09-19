# `minio-presigned-put-proxy`｜正式环境预签名 PUT 代理修复

## 目标

修复正式 H5 头像上传中 `/minio-api/` 预签名 PUT 返回 403 的问题，保持开发环境、文件 API、对象 Key 和权限模型不变。

## 已确认事实

- `/haji-api/api/app/files/upload-url` 返回 200。
- 紧接的 `/minio-api/...` PUT 返回 403。
- 后端 `MINIO_ENDPOINT`、公网前缀、Bucket、时间和 MinIO 可用性均已核对。
- 失败 PUT 的 SigV4 签名按日志参数匹配内网 Host `172.20.0.1:9000`。
- 线上 Nginx `/minio-api/` 缺少清空 `Connection` 和关闭 chunked transfer 的配置。

## 修改范围

- 补齐 Nginx MinIO S3 API 代理的 Host、Connection、chunked transfer、请求体大小和超时配置。
- 不修改数据库、文件对象、MinIO 密钥或前端业务逻辑。

## 当前状态

- 仓库 Nginx 示例：已修改。
- 正式 Nginx：已备份原配置，应用修改后 `nginx -t` PASS，并已 reload。
- 正式 MinIO 代理探针：通过正式域名生成同类 SigV4 PUT，HTTP 200；MinIO 读回 PASS；临时对象清理 PASS。
- 正式头像上传回归：PASS；真实页面已取得 `upload-url 200`、MinIO PUT `200`、`complete 200`、`view-url 200` 和对象 GET `200` 的连续证据。个人资料保存按钮未在本次验收中执行，不影响文件上传链路结论。
