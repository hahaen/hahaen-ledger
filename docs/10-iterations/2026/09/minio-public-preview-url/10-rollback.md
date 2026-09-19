# 回滚

如需回滚，恢复 `MinioStorageService` 的原预签名返回逻辑，并撤销生产配置中的必填 `MINIO_PUBLIC_URL_PREFIX`；正式客户端无法访问内网地址的问题也会恢复。Nginx 示例可单独撤销新增的 `/minio-api/` location。无数据库或对象数据回滚步骤。
