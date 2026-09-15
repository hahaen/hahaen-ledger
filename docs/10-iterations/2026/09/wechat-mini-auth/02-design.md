# 设计

小程序端调用 `uni.login({ provider: 'weixin' })`，只提交 `code` 到业务后端。后端通过 `WechatCode2SessionClient` 调用微信 `jscode2session`，校验返回结果后以 `(WECHAT_MINI_PROGRAM, openid)` 查询 `user_identity`。

已存在身份时读取有效的 `app_user`；首次登录时创建没有 H5 账号和密码的本地用户，并创建身份关联。登录成功后由 Sa-Token 生成业务 token，前端仍通过统一 API 请求封装发送 `X-Auth-Token`。

`session_key` 只在服务端换码响应内使用，不进入数据库和响应体；当前不需要微信数据解密，因此不持久化。

微信 API 调用失败不返回微信原始错误信息，服务端只记录稳定的错误编码和 Trace ID。
