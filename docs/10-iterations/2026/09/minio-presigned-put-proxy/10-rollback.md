# Rollback

本次原配置备份为 `/home/hahaen/nginx/conf.d/default.conf.bak-20260920-minio-presigned-put`。若新配置导致异常，在服务器上恢复该文件到 `/home/hahaen/nginx/conf.d/default.conf`，然后执行 `docker exec nginx nginx -t`；检查通过后执行 `docker exec nginx nginx -s reload`。本次不涉及数据库或业务对象数据回滚。
