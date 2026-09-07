# API 核对

当前 Controller 暴露的接口已整理到 `docs/03-api/README.md`。核对结果：

- H5 路径为 `/api/app/auth/h5/register`、`/api/app/auth/h5/login`，不是历史原型档案中的 `/api/app/auth/login`。
- 账单、退款、账户、资产、首页、日历、文件和资料接口均可在当前 OpenAPI 运行探针中看到。
- `ApiResponse`、`X-Auth-Token`、金额分字段、`idempotencyKey` 和 `AUTH_REQUIRED` 规则已写入当前规范。
- 前端小程序仍请求不存在的 `/api/app/auth/login`，这是当前已确认的 FAIL，而不是文档润色问题。

