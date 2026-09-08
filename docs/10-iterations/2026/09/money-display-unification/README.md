# `money-display-unification`｜前端金额展示统一

## 迭代目的

统一 uni-app 前端所有运行时金额展示的格式化规则：金额以整数分传输和计算，展示时 `.00` 不显示，其余两位以内有效小数保留；空值安全降级为 `¥0`。不修改后端接口、数据库字段或金额计算精度。

## 档案导航

- [01-requirement](01-requirement.md)
- [02-design](02-design.md)
- [03-database](03-database.md)
- [04-api](04-api.md)
- [05-backend](05-backend.md)
- [06-frontend](06-frontend.md)
- [07-testing](07-testing.md)
- [08-commands](08-commands.md)
- [09-verification](09-verification.md)
- [10-rollback](10-rollback.md)
