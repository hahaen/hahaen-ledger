# 独立物品 API

用户归属由Sa-Token当前身份决定，忽略任何前端userId，不关联账本/账户/账单。响应包复用ApiResponse，ID返回字符串。物品金额为0～99,999,999,999整数分；日均成本为BigDecimal分（8位小数），展示换算元并四舍五入至0.01。日期YYYY-MM-DD、2000-01-01起、不能晚于项目Asia/Shanghai当天。名称trim后1～40个Unicode字符。

## 查询

GET /api/app/items?status=ACTIVE&page=1&pageSize=20。status=ACTIVE|RETIRED|ALL，page>=1，pageSize=1～100；全部清单先按状态排序，在役物品优先，再按购买日期倒序（购买时间短的在上、长的在下），同日按ID倒序。ACTIVE/RETIRED筛选沿用对应组内购买日期倒序。响应包含asOf、totalAssetsCents、totalDailyCostCents、activeCount、retiredCount、items、total、page、pageSize、hasMore。摘要永远只统计在役物品，与筛选/页码无关。总资产为购入价之和，不是市场估值；总日资产为各物品当前日均成本之和。列表条目为ItemVO：id,name,priceCents,purchasedOn,status,retiredOn,resaleCents,serviceDays,netCostCents,dailyCostCents。清单价格位展示 `netCostCents`：在役等于购入价，退役等于购入价减二手售价，可为负数；`priceCents` 仍表示原始购入价。

GET /api/app/items/{id}返回item、asOf、costHistory；每个点含date、day、dailyCostCents，最多31点，包含购买日和今天/退役日两端，横轴为自然日期，非未来预测。购买/退役当天计入服役天数；退役之前按购入价/day计算，退役终点按净成本/day计算。单日退役只有一个净成本点。

## 创建

POST /api/app/items

```json
{
  "name": "耳机",
  "priceCents": 89900,
  "purchasedOn": "2026-09-01",
  "serving": true,
  "retiredOn": null,
  "resaleCents": null,
  "idempotencyKey": "item_example_create_001"
}
```

serving=false须提供retiredOn与resaleCents；serving=true禁止附带退役信息。retiredOn须在购买日至今天之间。售价允许0（未出售），也允许高于原价（净成本为负）。创建相同键相同原始内容重试返回当前记录，生命周期变化不导致重复创建；相同键不同内容返回IDEMPOTENCY_CONFLICT；删除后原创建键不能用于复活或新建。

## 退役

POST /api/app/items/{id}/retire

```json
{ "retiredOn": "2026-09-28", "resaleCents": 20000, "idempotencyKey": "item_example_retire_001" }
```

同一物品退役同键同内容重试成功；已退役物品不同键或不同日期/金额返回IDEMPOTENCY_CONFLICT，不再次写入。最终净成本=购入价-二手售价；日均净成本=净成本/含起止日服役天数，之后冻结。不自动写入收入或支出。

## 重新服役

POST /api/app/items/{id}/reactivate

```json
{ "idempotencyKey": "item_example_reactivate_001" }
```

仅已退役且属于当前用户的物品可操作。成功后状态为ACTIVE，`retiredOn`、`resaleCents` 清空为null；服役天数从原购买日期连续计算至今天，净成本恢复为购入价，日均成本与历史图据此重算。相同键在仍在役时重试返回当前物品；新的键用于在役物品或旧键在物品再次退役后重放，均返回IDEMPOTENCY_CONFLICT。最近一次退役请求键在重新服役后不能用于再次退役。不生成账单或改变账户余额。

## 编辑资料

PUT /api/app/items/{id}，仅修改名称、购买价格和购买日期，不改变服役状态、退役日期或二手售价。

```json
{
  "name": "降噪耳机",
  "priceCents": 89900,
  "purchasedOn": "2026-09-01",
  "idempotencyKey": "item_example_edit_001"
}
```

金额、名称和日期校验沿用创建规则；购买日期不能晚于今天或既有退役日期。编辑按用户归属并要求目标未删除。用户内幂等键同内容重试成功，异内容冲突；成功后详情成本按新资料重算，不生成账单。

## 删除

DELETE /api/app/items/{id}?idempotencyKey=item_example_delete_001。记录逻辑删除并写deleted_at/deleted_by/deleted_name和更新审计。相同键重试成功，已删除目标不同删除键返回ITEM_NOT_FOUND；所有普通查询/汇总排除已删除数据。

## 幂等与权限

所有写键使用[A-Za-z0-9_-]{16,64}；按用户锁串行化。同一创建键在用户内唯一且物品删除后保留。重新服役键在用户内唯一；退役/删除键限定目标操作，不是跨接口全局键。未知/跨用户ID统一ITEM_NOT_FOUND，不泄露是否存在。输入失败返回已有统一400错误包；未登录按现有认证拦截返回401。保存超时须保留原键重试。
