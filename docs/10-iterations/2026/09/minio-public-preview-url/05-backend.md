# 后端

`MinioStorageService` 保留一个以 `MINIO_ENDPOINT` 创建的 MinIO 客户端。预签名 PUT 和 GET 地址生成后，校验生成地址的 origin 仍为内部 Endpoint，再将地址拼接到配置的公网前缀；prod Profile 缺少该前缀时启动失败。对象 PUT/GET、stat、删除及建桶行为不变。
