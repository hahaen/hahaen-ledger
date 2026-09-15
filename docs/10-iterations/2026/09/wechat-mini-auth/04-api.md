# API

## 新增接口

`POST /api/app/auth/wechat-mini/login`

请求：

```json
{
  "code": "uni.login 返回的一次性凭证"
}
```

响应沿用 `LoginVO`：`token`、`userId`、`nickname`。

错误编码：

- `WECHAT_LOGIN_DISABLED`
- `WECHAT_CONFIG_INVALID`
- `WECHAT_CODE_INVALID`
- `WECHAT_API_UNAVAILABLE`
- `WECHAT_IDENTITY_CONFLICT`
- `WECHAT_USER_UNAVAILABLE`

接口不接收 `open_id`、`union_id`、`userId` 或前端昵称作为身份依据。
