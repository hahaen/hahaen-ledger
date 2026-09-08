# `account-id-precision-fix`｜账户点击 ID 精度修复

## 迭代目的

修复资产页、账户详情、账单详情之间传递账户/流水 ID 时的精度丢失问题，避免点击列表数据后因 ID 被 JavaScript Number 舍入而返回“账户不存在”。

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
