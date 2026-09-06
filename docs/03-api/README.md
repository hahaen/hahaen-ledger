# API 规范

统一响应为 `{code,message,data}`，成功 `code=0`。认证使用 `X-Auth-Token`。金额传整数分，时间传 ISO `LocalDateTime`。所有账户、账单、退款接口从当前用户的当前账本过滤，客户端不得传入或依赖 `userId` 作为权限依据。完整路径见现有 `api.md`，变更必须同步 iteration 和 TypeScript 使用方。
