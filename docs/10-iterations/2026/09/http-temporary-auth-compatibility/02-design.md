# 设计

浏览器具备 Web Crypto 时始终使用既有 RSA-OAEP 加密。仅当浏览器缺少 Web Crypto 且服务端通过配置明确声明允许时，认证页对密码 UTF-8 字节进行可逆异或混淆并提交独立的 `compatibilityPassword` 字段。

服务端只在以下条件同时满足时接收该字段：开关开启、请求不是 HTTPS、`compatibilityPassword` 与 `encryptedPassword` 恰有一个有值。其余情况一律拒绝。个人中心设置或修改密码不纳入兼容范围。

该混淆仅避免 HTTP 请求正文直接出现密码原文，不能提供机密性或完整性；HTTP 中间人仍可读取或篡改脚本和载荷，必须在 HTTPS 可用后关闭此开关。
