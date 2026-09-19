# API

接口路径、DTO/VO 和响应字段均未变化。现有 `POST /api/app/files/upload-url` 请求中的 `originalName` 用于生成新对象 Key。

预签名 URL 仍由服务端生成并仅短时有效；客户端不接收 Bucket 密钥或永久对象 URL。上传归属及后续完成、预览、删除权限保持当前用户校验。
