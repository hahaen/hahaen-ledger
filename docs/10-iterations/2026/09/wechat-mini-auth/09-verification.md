# 验证

| 范围 | 状态 | 证据 |
| --- | --- | --- |
| 微信后端 code2Session | PASS（本机运行探针） | 使用本地已配置 AppID/Secret 发送无效 code，微信真实返回 `errcode=40029`，后端正确映射为“微信登录凭证无效”；同时修复了微信 `text/plain` JSON 响应导致的 `UnknownContentTypeException` |
| 微信身份首次创建/复用 | PASS（单测） | `WechatMiniAuthServiceTest` 覆盖首次创建和已有身份复用 |
| 微信配置与基础依赖 | PASS（本机运行） | manifest AppID 与后端本地配置一致；后端 8080、MySQL 3306、Redis 6379、MinIO 9000 可用，Flyway 已到 V4 |
| H5 认证回归 | PASS（静态+前端回归） | H5 API 路径未改变，认证页仍调用 `/api/app/auth/h5/login` |
| 后端测试 | PASS | `mvn test`：47/47 |
| 前端回归 | PASS | `node --test tests/*.test.mjs`：42/42，包含会话失效后重登录并只重试一次原请求 |
| 前端类型检查与双端构建 | PASS | `pnpm run typecheck`、注入本地 API 地址后 H5 和微信小程序构建均完成 |
| 微信开发者工具真实首次登录 | PASS（运行态） | 使用本机微信开发者工具自动运行真实小程序，数据库从无身份关联创建为 1 条，产生成功登录审计 |
| 微信开发者工具真实重复登录 | PASS（运行态） | 两次成功登录审计对应同一个系统用户，`user_identity` 仍只有 1 条未删除关联，未重复创建用户 |
| 真实 Token 与后续业务接口 | PASS（运行态） | 使用开发者工具真实登录产生的 Token 访问 `/api/app/accounts`、`/api/app/user/profile`，均返回 HTTP 200；Token 未写入文档或日志 |
| 真实 Token 失效自动重登录 | PASS（运行态） | 删除当前测试会话的 Redis Token 映射后重新触发小程序，成功登录数从 2 增至 3，仍命中同一系统用户；新 Token 再访问账户/资料接口均 HTTP 200 |
