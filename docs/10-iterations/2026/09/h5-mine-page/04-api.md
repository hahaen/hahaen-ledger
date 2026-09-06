# API

## `GET /api/app/user/profile`

需要当前登录用户会话，无请求参数。

返回：`userId`、`nickname`、`createdAt`、`cumulativeDays`、`avatarAuthorized`、`avatarFileId`。

错误：`AUTH_REQUIRED`、`USER_NOT_FOUND`。

## 头像接口（复用）

- `POST /api/app/files/upload-url`：业务类型固定为 `AVATAR`，返回 10 分钟预签名 PUT URL。
- `PUT <uploadUrl>`：H5 浏览器直接上传二进制，不携带 MinIO 密钥。
- `POST /api/app/files/{fileId}/complete`：校验对象大小和 MIME 类型并关联当前用户头像。
- `GET /api/app/files/{fileId}/view-url`：返回短时效预览 URL。
- `GET /api/app/files/avatar/view-url`：当前用户头像预览 URL。

## 退出登录

复用 `POST /api/app/auth/logout`。H5 前端无论服务端响应成功或失败都清理本地会话，避免继续使用旧令牌；本轮页面保持在我的页。
