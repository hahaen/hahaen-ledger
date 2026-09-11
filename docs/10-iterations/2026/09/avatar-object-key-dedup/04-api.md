# API

`POST /api/app/files/upload-url` 的 `fileHash` 现在必填且必须为 64 位 SHA-256 十六进制值。命中同用户已就绪头像时返回原 `fileId` 和 `status=READY`，客户端直接请求预览接口。

`POST /api/app/files/{fileId}/complete` 只验证对象内容并把文件置为 `READY`，不会更新用户当前头像。资料保存 `PUT /api/app/user/profile` 可选携带 `avatarFileId`；服务端仅接受当前用户自己的、`AVATAR`、`READY` 且未删除的文件，并在该资料保存事务中将其对象 Key 写入 `avatarFileUrl`。资料响应使用 `avatarFileUrl`（对象 Key）；文件预览响应增加 `objectKey`，仍不返回永久对象 URL。
