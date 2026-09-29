# API

接口路径、参数、响应字段、权限与幂等规则均不变。`GET /api/app/items` 的清单顺序调整为：ACTIVE 优先，其后 RETIRED；每组内 `purchasedOn` 倒序、ID 倒序。ACTIVE/RETIRED 单状态筛选仍按购买日期和 ID 倒序。
