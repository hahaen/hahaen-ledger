# 03｜API 契约

本目录描述当前 Controller 暴露的 HTTP 契约。路径和字段以 `server/src/main/java/**/controller`、DTO/VO 与实际 OpenAPI 为准；产品说明中的“应支持”不能替代当前接口事实。

## 通用协议

- 基础路径：`/api/app`。
- 成功响应：`{ "code": 0, "message": "success", "data": ... }`。
- 业务错误：通常 HTTP 400，响应 `code=1`；未登录的 `AUTH_REQUIRED` 为 HTTP 401；参数校验错误为 HTTP 400、`code=400`；未处理异常为 HTTP 500、`code=500`。
- 认证请求头：`X-Auth-Token`。当前用户由 Sa-Token 会话解析，接口不接受 `userId` 作为权限依据。
- 金额字段统一为整数分，名称使用 `*Cents`；日期时间请求使用无时区 ISO 本地日期时间字符串（例如 `2026-09-08T12:30`），服务端业务统计按 `Asia/Shanghai` 的自然日/月处理。
- 账单创建、还款和退款写请求应携带 `idempotencyKey`；当前 Service 对创建账单和退款执行幂等查询。账户 CRUD、H5 认证和账单编辑当前没有统一的幂等字段，重复提交边界必须在后续接口审计中单独确认。

## 当前接口清单

### 认证与用户

| 方法 | 路径 | 当前用途 |
| --- | --- | --- |
| GET | `/api/app/auth/password-key` | 获取 H5 密码 RSA 公钥 |
| GET | `/api/app/auth/captcha` | 创建一次性、5 分钟有效的图形验证码 |
| POST | `/api/app/auth/h5/register` | 注册 H5 账号；请求含 `account`、`encryptedPassword`、`captchaId`、`captchaCode` |
| POST | `/api/app/auth/h5/login` | 登录 H5 账号并返回 `token`、`userId`、`nickname` |
| POST | `/api/app/auth/logout` | 注销当前会话 |
| GET | `/api/app/user/profile` | 返回当前用户资料、创建时间、从最早有效账单业务日期起算的累计记账天数、头像状态和 `passwordConfigured`；没有有效账单时天数为 0 |
| PUT | `/api/app/user/profile` | 更新当前用户昵称和首次登录账号；首次无密码时请求还须含 RSA-OAEP 密文 `encryptedPassword`；选择新头像时额外传已完成上传的 `avatarFileId` |
| PUT | `/api/app/user/profile/password` | 更新当前用户密码；请求含 RSA-OAEP 加密后的 `encryptedPassword`，首次设置账号时同时传 `loginAccount` |

当前后端没有 `/api/app/auth/login`。前端 `ledger.login()` 的小程序分支仍调用该旧路径，因此微信自动登录暂不能标记为接口闭环。

H5 账号服务端会 trim 并转为小写，格式为 2–64 位小写字母/数字/`.`/`_`/`-`；客户端提交的是 RSA-OAEP 加密后的 `encryptedPassword`，解密后的密码要求 8–64 个字符且 UTF-8 不超过 72 字节。验证码校验成功后一次性消费，注册成功不会自动返回会话。

`GET /user/profile` 额外返回 `loginAccount` 和不含敏感信息的 `passwordConfigured`。已设置账号时资料更新接口只接受相同账号，任何改写均返回“账号已设置，不能修改”。账号和密码均为空的用户，须在首次资料保存时一并提供账号和加密密码，服务端在同一事务中写入；已有密码仍通过独立密码接口即时更新。服务端以当前会话用户过滤，并在应用层预检后由 `app_user.uk_app_user_login_account` 唯一索引兜底重复账号。资料和密码接口均不接受 `userId`。

### 首页、日历与资产

| 方法 | 路径 | 当前用途 |
| --- | --- | --- |
| GET | `/api/app/home/summary?month=YYYY-MM` | 月度支出、收入、结余、日均支出和当月账单 |
| GET | `/api/app/home/recent-transactions?beforeMonth=YYYY-MM` | 首页最近记账的双月分段查询；首次省略参数，后续以返回起始月作为排他游标 |
| GET | `/api/app/calendar?year=&month=` | 返回 42 格月历及每日收支标记 |
| GET | `/api/app/calendar/{date}` | 返回单日支出、收入、结余和账单 |
| GET | `/api/app/assets/overview` | 返回总资产、总负债、净资产及账户列表 |

首页日均支出当前按当月截至今天（含今天）或已结束月份天数作分母；未来月份分母为 0。转账和还款可出现在账单列表，但不计入收入/支出汇总。

最近记账首次返回业务时区当前月与上月。响应字段 `startMonth`、`endMonth` 标识本段覆盖范围，`hasMore` 表示这两个月之前是否仍有当前用户的未删除账单；后续请求把 `startMonth` 作为 `beforeMonth`，服务端按排他上界返回更早的连续两个月。

### 账户

| 方法 | 路径 | 请求/查询重点 |
| --- | --- | --- |
| GET | `/api/app/accounts` | 当前用户未删除账户 |
| POST | `/api/app/accounts` | `name`、`kind`、资金账户 `balanceCents`，或信贷账户 `creditLimitCents`/`currentDebtCents`，可选 `includedInNetAsset` |
| GET | `/api/app/accounts/{id}` | 当前用户账户详情 |
| PUT | `/api/app/accounts/{id}` | 编辑名称、金额和净资产标识；账户类型不可修改 |
| PUT | `/api/app/accounts/{id}/order` | `targetAccountId`、双方当前 `expectedSortOrder`/`targetExpectedSortOrder`、`idempotencyKey`；锁定并交换同类账户顺序 |
| DELETE | `/api/app/accounts/{id}` | 写入删除审计字段并逻辑删除；当前没有独立停用状态 |
| GET | `/api/app/accounts/{id}/transactions?type=&page=&pageSize=` | 账户流水分页，页大小服务端限制为 1–100 |
| POST | `/api/app/accounts/{id}/repayments` | `fundAccountId`、`amountCents`、`idempotencyKey`；目标 `{id}` 必须为信贷账户 |

数据库没有账户名称唯一索引，但当前 `AccountService` 对同一用户的有效账户执行重名校验；接口返回的 `status=ACTIVE` 是 VO 展示字段，不是 `asset_account` 的物理列。

### 账单与退款

| 方法 | 路径 | 请求/查询重点 |
| --- | --- | --- |
| GET | `/api/app/transactions?month=&date=&accountId=&type=&page=&pageSize=` | 按月份、日期、账户、类型分页查询；月份和日期不能同时传 |
| POST | `/api/app/transactions` | 创建 `EXPENSE`、`INCOME`、`TRANSFER`、`REPAYMENT` |
| GET | `/api/app/transactions/{id}` | 账单、有效退款累计、当前有效金额和退款列表 |
| PUT | `/api/app/transactions/{id}` | 编辑账单并在事务中撤销旧余额影响后应用新影响 |
| DELETE | `/api/app/transactions/{id}` | 逻辑删除账单及有效退款，并恢复账户影响 |
| POST | `/api/app/transactions/{id}/refunds` | 仅支出/收入可退款；`amountCents` 不得超过剩余可退款金额 |
| DELETE | `/api/app/transactions/refunds/{refundId}` | 逻辑删除退款并重算原账单有效金额 |

账单请求字段为 `type`、`amountCents`、`accountId`、`fromAccountId`、`toAccountId`、`occurredAt`、`note`、`idempotencyKey`。支出/收入使用 `accountId`；转账使用两个不同的资金账户；还款使用资金账户到信贷账户。退款不是账单类型。

### 文件

| 方法 | 路径 | 当前用途 |
| --- | --- | --- |
| POST | `/api/app/files/upload-url` | 创建文件元数据并返回短时效 PUT URL；`fileHash` 为必传 SHA-256；同摘要已就绪头像直接返回 `READY`，前端不再 PUT |
| POST | `/api/app/files/{fileId}/complete` | 校验对象大小、SHA-256、声明 MIME 与图片文件头后确认文件为 READY，不替换当前头像 |
| GET | `/api/app/files/{fileId}/view-url` | 当前用户文件的短时效预览 URL |
| GET | `/api/app/files/avatar/view-url` | 当前用户头像预览 URL |
| DELETE | `/api/app/files/{fileId}` | 删除对象并逻辑删除元数据 |

账单附件的数据库枚举已预留 `TRANSACTION_ATTACHMENT`，但当前业务 Service/前端不应把它当成首版可用能力。

资料响应的 `avatarFileUrl` 是稳定的 MinIO 对象 Key，不是完整 URL；页面只能通过头像预览接口取得短时效 `viewUrl`。文件预览响应同时返回当前用户自己的 `objectKey`，用于刷新前端资料状态，不能拿它拼接 MinIO 地址。

## 维护规则

接口变化必须同步 DTO/VO、`app/src/utils/api.ts` 或文件工具、页面 loading/失败重试、权限测试和对应迭代档案。没有真实接口联调证据时，只能记录 `PARTIAL`、`BLOCKED` 或 `NOT_RUN`。

## 交易编辑与退款边界补充（2026-09-08）

PUT transactions/{id} 不覆盖原创建 idempotencyKey；还款编辑在事务内先撤销旧影响再按恢复后的余额/欠款校验。退款幂等键重复但金额不同返回 IDEMPOTENCY_CONFLICT。删除退款取得原账单锁后必须重新锁定读取有效退款，防止等待期间已删除的记录再次扣款。occurredAt 年份限定 1000–9999，以匹配 MySQL DATETIME 范围。
