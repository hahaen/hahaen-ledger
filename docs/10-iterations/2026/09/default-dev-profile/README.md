# `default-dev-profile`｜开发 Profile 默认配置

## 迭代目的

本迭代用于明确 Spring Boot 默认激活的开发 Profile、开发数据库、Redis 和 MinIO 的隔离方式，让本地启动有稳定入口，同时不把生产凭证或环境名称写进业务 Key。

## 应记录的重点

- `application.yml` 的默认 Profile 和 `application-dev.example.yml` 的复制方式。
- MySQL、Redis、MinIO 的开发连接前提与 Bucket/端口边界。
- 真实 `application-dev.yml` 被忽略，example 不含秘密。
- Flyway、Redis Key 和 MinIO 配置在开发/生产之间如何隔离。

## 当前档案状态

当前工作区未保留本迭代明细文件；这里只说明审查范围，不把本地服务可启动推断为生产配置已验证。
