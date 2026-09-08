# API

以下响应字段由 JSON 数字调整为 JSON 字符串：账户 `id`，流水 `id/accountId/fromAccountId/toAccountId`，退款 `id`，登录 `userId`，个人资料 `userId/avatarFileId`，文件上传/完成/查看响应的 `fileId`。

请求路径参数和业务请求体中的 ID 仍由后端按 `Long` 接收，不改变接口对应的数据库主键和权限归属校验。
