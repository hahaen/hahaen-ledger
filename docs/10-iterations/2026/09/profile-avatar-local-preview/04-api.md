# API

不新增或修改 API。头像上传仍使用既有文件上传与完成接口；页面仅在用户点击保存时调用既有 `PUT /api/app/user/profile` 并按需附带 `avatarFileId`。
