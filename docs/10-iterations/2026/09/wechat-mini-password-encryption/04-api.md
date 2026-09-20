# API

保持既有接口不变：前端继续从 `GET /api/app/auth/password-key` 获取 SPKI 公钥，个人中心资料与密码接口继续只提交 RSA-OAEP 密文 `encryptedPassword`。

