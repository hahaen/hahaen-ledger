# 后端

`ProfileController` 新增当前用户资料和密码更新接口；`ProfileService` 通过 `CurrentUser.id()` 查询有效用户，绝不接收前端用户 ID。资料更新只允许修改昵称及首次账号；已设置账号与传入值不一致时拒绝。首次账号/密码更新在一个事务内完成，先作格式和唯一性预检，再由现有唯一索引处理并发竞争；密码仅由 `PasswordCryptoService` 解密并交给 `PasswordEncoder` 哈希。

`ProfileServiceTest` 覆盖昵称更新、账号锁定、首次账号与密码同时更新、重复账号拒绝、累计天数的原有边界。

资料响应新增 `passwordConfigured` 状态，避免前端从账号字段推断密码状态。首次资料保存改为接收同一请求中的加密密码，事务内写入账号和密码；已有密码继续只允许经独立密码接口更新。
