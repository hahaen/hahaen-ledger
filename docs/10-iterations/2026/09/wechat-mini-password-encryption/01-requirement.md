# Requirement

- 微信小程序个人中心首次设置密码和修改密码必须能够生成既有 `encryptedPassword` 密文并正常提交。
- 不向请求、日志或本地存储写入明文密码。
- 不改变 H5 的 Web Crypto 加密路径、后端接口或数据库结构。

