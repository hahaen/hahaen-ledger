# 设计

## 前端

- 启动时从持久化存储恢复 token 和用户快照。
- H5 有效会话只执行一次数据预加载，不调用 `reLaunch` 改写当前路由。
- 仅当当前路径为空根路径时进入首页；认证页保持直接访问行为。
- 统一处理 401/403：清理内存状态和持久化会话，并回到登录页。

## 后端

- 引入 Sa-Token Redis Jackson DAO。
- 通过 Spring Bean 显式绑定现有 `RedisConnectionFactory`，避免普通 Spring Data Redis 依赖被误认为已启用 Sa-Token Redis 存储。
- DAO 对 Sa-Token 原始 Key 增加 `haji:` 前缀，搜索结果去除该内部前缀。

## 关键决策

- 不把路由写入额外存储：H5 hash 本身就是刷新后可恢复的当前路由来源。
- 不自动刷新 token 或延长会话；继续使用服务端 30 天有效期和 Sa-Token 原有 TTL 机制。
- 不修改数据库结构，不在前端保存密码、密钥或其他敏感凭证。
