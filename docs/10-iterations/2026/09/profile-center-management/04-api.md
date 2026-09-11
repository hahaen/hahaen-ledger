# API

本次新增当前用户资料读取字段、资料更新接口和密码更新接口。请求不接受 `userId`；头像继续复用既有文件上传完成时绑定当前用户的机制。

`GET /api/app/user/profile` 返回布尔字段 `passwordConfigured`，仅表达当前用户是否已经设置密码，不返回密码哈希。首次保存资料时，`PUT /api/app/user/profile` 须同时提供 `nickname`、`loginAccount` 和 RSA-OAEP 加密后的 `encryptedPassword`；服务端在同一事务内完成账号、密码和昵称写入。已有密码时该字段不参与资料更新；`PUT /api/app/user/profile/password` 继续用于即时修改密码。
