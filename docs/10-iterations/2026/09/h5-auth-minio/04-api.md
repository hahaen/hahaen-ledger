# API

- `GET /api/app/auth/password-key`：返回 RSA-OAEP 公钥。
- `GET /api/app/auth/captcha`：创建图形验证码。
- `POST /api/app/auth/h5/register`：注册 H5 账号。
- `POST /api/app/auth/h5/login`：登录 H5 账号。
- `POST /api/app/auth/logout`：注销当前会话。
- `POST /api/app/files/upload-url`：创建头像上传元数据并返回短时效 PUT URL。
- `POST /api/app/files/{fileId}/complete`：确认对象存在并标记 READY。
- `GET /api/app/files/{fileId}/view-url`：返回短时效 GET URL。
- `DELETE /api/app/files/{fileId}`：删除对象并逻辑删除元数据。

认证和文件接口的错误统一使用 `ApiResponse`；服务端不接受前端传入 `userId` 作为权限依据。
