# 回滚

- 前端回滚本迭代涉及的 `App.vue`、`ledger.ts` 和 `api.ts` 文件级改动。
- 后端回滚 Sa-Token Redis DAO 依赖、配置类和依赖声明；回滚前确认没有依赖 Redis 会话的在线实例切换风险。
- 本次无数据库迁移，不执行 `git reset --hard`、清库或 Redis 清库操作。
