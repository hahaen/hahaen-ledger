# API

状态：`NOT_RUN`。本次不实现接口。

后续规划：

- H5 登录/注册使用 `app_user.login_account` 和 `password_hash`。
- 微信自动登录通过 `user_identity(provider, open_id)` 查询用户。
- H5 登录用户生成一次性绑定凭证，小程序使用微信 `code` 完成身份绑定。
- 已绑定其他用户的微信身份禁止自动合并。
