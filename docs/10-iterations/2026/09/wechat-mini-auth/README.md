# `wechat-mini-auth`｜微信小程序默认登录

本迭代为微信小程序接入默认静默登录：小程序通过 `uni.login` 获取一次性 code，后端服务端向微信换取身份标识，按 `user_identity` 查找或创建本地用户，再签发既有 Sa-Token。H5 继续使用账号、密码和图形验证码认证。

当前范围不包含微信昵称、头像、手机号、支付、订阅消息或用户资料授权。

## 状态

- 代码实现：已完成
- 数据库：沿用现有 `user_identity` 和 `app_login_log`，无需新增 Migration
- 本机后端与微信服务无效 code 探针：已通过
- 真实微信开发者工具首次/重复登录联调：已通过
- 真实 Token 失效自动重登录与业务接口：已通过

## 档案导航

见 `01-requirement.md` 至 `10-rollback.md`。
