# 设计

认证链路：H5 获取公钥和验证码 → 浏览器加密密码 → 服务端解密并校验 → BCrypt 校验/保存 → Sa-Token 登录。

验证码答案存 Redis，Key 统一使用 `haji:auth:captcha:{captchaId}`，TTL 为 5 分钟且只允许消费一次。密码 RSA 密钥对由配置的私钥派生公钥；配置默认值为无秘密的 `GENERATE`，未提供 `H5_PASSWORD_RSA_PRIVATE_KEY` 时生成进程级密钥对，生产环境应配置固定私钥。

文件链路：服务端创建 `UPLOADING` 元数据并返回 10 分钟 PUT 预签名 URL → 浏览器直传 MinIO → 服务端 `complete` 通过 MinIO `statObject` 校验后标记 `READY` → 预览接口返回 10 分钟 GET 预签名 URL。删除先删除对象，再以 `DELETED` + 数据库逻辑删除结束。
