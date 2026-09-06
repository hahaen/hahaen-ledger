# API

业务 API 路径和请求/响应结构不变。

- H5 登录继续返回 `token`、`userId`、`nickname`。
- 前端继续通过 `X-Auth-Token` 发送 token。
- 认证失败仍由服务端返回 401/403，前端统一清理本地会话。
- Redis DAO 只改变会话存储位置，不改变对外协议。
