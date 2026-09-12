# 前端

认证页在可用时继续调用既有 RSA-OAEP 加密函数；无 Web Crypto 时读取服务端开关，得到许可才构造 `compatibilityPassword`，请求 JSON 不出现密码原文。个人中心继续使用原加密函数，因此 HTTP 下改密仍会提示使用 HTTPS。
