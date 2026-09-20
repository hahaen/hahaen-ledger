# Design

浏览器继续使用 `crypto.subtle` 执行 RSA-OAEP（SHA-256）。微信小程序在该 API 不存在时，使用兼容实现执行同一算法，并通过微信运行时的密码学安全随机数 API 生成 OAEP 种子。两条路径使用同一个服务端 SPKI 公钥并输出 Base64 密文。

