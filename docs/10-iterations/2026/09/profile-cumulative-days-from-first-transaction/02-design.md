# 设计

## 统计口径

`cumulativeDays = DAYS_BETWEEN(DATE(MIN(active occurred_at)), today) + 1`。

服务端以 `Asia/Shanghai` 进程业务日期取得 `today`；账单的 `occurred_at` 是既有业务记账时间，不以记录写入数据库的 `created_at` 替代。

## 边界

| 情况 | 返回 |
| --- | --- |
| 无有效账单 | 0 |
| 最早账单在当天 | 1 |
| 最早账单在昨天 | 2 |
| 最早账单日期晚于今天 | 0 |

选择 `MIN(occurred_at)` 而不是前端拉取账单，是为了保证用户隔离、逻辑删除过滤和 API 语义集中在服务端。当前项目为单用户单账本模型，按当前用户范围计算。
