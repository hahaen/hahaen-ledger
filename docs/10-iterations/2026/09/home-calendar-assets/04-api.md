# 04｜API

统一响应为现有 `ApiResponse`。所有接口从 Sa-Token 当前用户取 `userId`，请求体不接受可用于权限判断的前端用户标识。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/api/app/home/summary?month=YYYY-MM` | 首页月度概览和月内最近账单 |
| GET | `/api/app/transactions?month=YYYY-MM&date=YYYY-MM-DD&accountId=&type=` | 账单列表 |
| POST | `/api/app/transactions` | 创建支出/收入/转账/还款 |
| GET | `/api/app/transactions/{id}` | 账单详情和有效退款 |
| PUT | `/api/app/transactions/{id}` | 编辑账单 |
| DELETE | `/api/app/transactions/{id}` | 软删除账单 |
| POST | `/api/app/transactions/{id}/refunds` | 创建退款 |
| DELETE | `/api/app/transactions/refunds/{refundId}` | 软删除退款并重算有效金额 |
| GET | `/api/app/calendar?year=&month=` | 月历日期标记和月度账单 |
| GET | `/api/app/calendar/{date}` | 单日汇总和账单 |
| GET | `/api/app/assets/overview` | 资产三项汇总和账户列表 |
| GET | `/api/app/accounts` | 当前用户有效账户 |
| POST | `/api/app/accounts` | 新增账户 |
| GET | `/api/app/accounts/{id}` | 账户详情 |
| PUT | `/api/app/accounts/{id}` | 编辑账户 |
| DELETE | `/api/app/accounts/{id}` | 主动软删除/停用账户 |
| GET | `/api/app/accounts/{id}/transactions?type=` | 账户流水 |
| POST | `/api/app/accounts/{id}/repayments` | 信贷账户还款 |

金额均为整数分，日期使用 `LocalDateTime` 的设备本地值并按项目 `Asia/Shanghai` 口径切自然日/月；写请求要求 `idempotencyKey`，编辑可沿用现有记录幂等语义。

