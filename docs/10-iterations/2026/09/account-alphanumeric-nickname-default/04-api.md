# API

不新增接口或字段。以下既有写接口的 `account` / `loginAccount` 语义收紧为 2–64 位英文字母或数字：

- `POST /api/app/auth/h5/register`
- `POST /api/app/auth/h5/login`
- `PUT /api/app/user/profile`
- `PUT /api/app/user/profile/password`

非法中文或符号返回 `ACCOUNT_INVALID`；注册成功后，后续登录响应中的 `nickname` 为规范化后的注册账号。

