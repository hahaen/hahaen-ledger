# 后端

配置项为 `hahaen.auth.allow-insecure-password-over-http`，环境变量名为 `H5_ALLOW_INSECURE_PASSWORD_OVER_HTTP`，默认 `false`。开关只影响 H5 注册和登录，并保留验证码、账号、密码长度、BCrypt 和登录审计既有校验。服务端只在内存中解开兼容载荷后立刻交给既有 BCrypt 哈希流程，不记录密码或兼容载荷。

代理终止 TLS 时，控制器依据 `X-Forwarded-Proto: https` 识别 HTTPS；生产环境必须确保后端端口不直接暴露公网，避免伪造代理头。
