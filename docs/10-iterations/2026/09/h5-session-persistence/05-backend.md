# 后端

- `pom.xml` 增加 `sa-token-redis-jackson`，版本与 Sa-Token Starter 保持一致。
- 排除该依赖自带的 `SaTokenDaoForRedisTemplate` 自动配置，保证应用上下文只有一个 Sa-Token DAO Bean。
- 新增 `SaTokenRedisConfig`，注册 `SaTokenDaoForRedisTemplate`。
- 用 `PrefixedSaTokenDao` 统一将 Sa-Token Key 写入 `haji:` 命名空间，防止与其他环境或业务 Key 混用。
- 保留 `sa-token.timeout=2592000`，不通过无证据的方式延长会话。
