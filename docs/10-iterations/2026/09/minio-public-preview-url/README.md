# `minio-public-preview-url`｜正式环境文件回显地址修复

## 需求与结果

正式环境将 MinIO API 配置为容器/主机内网地址时，后端生成的短时签名 URL 也带内网 Host，H5/微信客户端无法访问。现在存储操作和签名仍使用 `MINIO_ENDPOINT`，对客户端返回前将签名 URL 的 origin/path 前缀映射到部署环境提供的 `MINIO_PUBLIC_URL_PREFIX`。

## 影响范围

- 后端只调整预签名 PUT/GET URL 的返回地址，文件 API 字段和文件元数据不变。
- 部署 Nginx 示例增加 `/minio-api/` 到 `http://172.20.0.1:9000/` 的路径代理。前缀移除后，路径回到签名时的对象路径；默认 `$proxy_host` 保留签名 Host。
- 无数据库、Flyway、前端业务或对象迁移。

## 验收状态

代码静态核对、构建、测试和正式运行态均按 `09-verification.md` 中的证据更新；未取得公网 H5/微信实际加载和 MinIO PUT/GET 证据前，不宣称生产验收通过。

具体档案按 01–10 顺序记录需求、设计、数据库、API、后端、前端、测试、命令、验证和回滚。
