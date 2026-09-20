# Frontend

- `passwordCrypto.ts` 保留 H5 Web Crypto 路径，并只在 `MP-WEIXIN` 条件下静态引用小程序兼容实现。
- `passwordCryptoMp.ts` 解析既有 SPKI 公钥，使用 node-forge 执行 RSA-OAEP、SHA-256、MGF1-SHA-256，并显式传入 `wx.getRandomValues` 返回的 32 字节种子。
- 安全随机数 API 不可用或失败时显示明确错误并停止提交，不使用弱随机数降级。
- `node-forge` 仅进入微信小程序产物，H5 产物继续使用原生 Web Crypto。
