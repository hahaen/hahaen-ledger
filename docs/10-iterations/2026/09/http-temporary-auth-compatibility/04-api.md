# API

`GET /api/app/auth/password-key` 增加 `insecurePasswordAllowed`，供 HTTP 浏览器判断是否可临时提交明文。

`POST /api/app/auth/h5/register` 与 `POST /api/app/auth/h5/login` 允许二选一：既有 `encryptedPassword`，或仅在开关开启且 HTTP 请求时的 `compatibilityPassword`。两个字段同时存在或都不存在均返回参数错误。
