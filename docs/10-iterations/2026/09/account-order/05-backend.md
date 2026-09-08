# 后端

- `AssetAccount`、`AccountVO` 增加 `sortOrder`。
- 创建账户在同类最大顺序后追加。
- `AssetAccountMapper` 按类型、顺序、ID返回，并提供创建时查询最大顺序和换序时的行锁查询。
- `AccountService.reorder` 校验当前用户、逻辑删除、账户类型和期望顺序，在单事务内更新两行。
- Controller 仅负责校验和协议适配，业务规则保留在 Service。
