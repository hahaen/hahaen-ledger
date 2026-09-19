# Commands

本文件只记录本轮实际执行且不包含密钥的命令。

- `git diff --check`：PASS。
- 读取线上 Nginx access/error 日志：`upload-url` 返回 200，紧接的 MinIO PUT 返回 403。
- 读取生产容器非敏感环境项：Endpoint 为 `http://172.20.0.1:9000`，公网前缀为 `/minio-api`，Bucket 为 `haji`。
- 本机 MinIO Client `mc admin info local`：PASS，MinIO 服务和管理员凭证可用。
- 对失败 PUT 的 SigV4 参数做离线校验：实际签名匹配内网 Host `172.20.0.1:9000`；未输出密钥或完整签名到文档。
- 正式服务器确认配置源为 `/home/hahaen/nginx/conf.d/default.conf`，先创建 `default.conf.bak-20260920-minio-presigned-put` 回滚备份。
- 正式 `/minio-api/` 补充 `proxy_set_header Connection ""`、`chunked_transfer_encoding off`、`client_max_body_size 0`；`docker exec nginx nginx -t`：PASS；随后执行 `docker exec nginx nginx -s reload`。
- 通过 `https://hahaen.xyz/minio-api/` 对随机临时对象执行同类 SigV4 PUT：HTTP 200；MinIO `mc stat` 读回 PASS；临时对象清理 PASS。
- reload 后 Nginx access log 记录该临时 PUT 为 HTTP 200；此前用户头像 PUT 记录仍为历史 403，不代表 reload 后结果。
- 用户在正式 H5 页面重试后，服务器 `2026-09-19 16:45:39 UTC` access log 脱敏核对：`/haji-api/api/app/files/upload-url` 200、`/minio-api/...avatar...png` PUT 200、`/haji-api/api/app/files/{id}/complete` 200、`/haji-api/api/app/files/{id}/view-url` 200、对象 GET 200。
