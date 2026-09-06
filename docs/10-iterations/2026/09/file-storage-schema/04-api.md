# API

状态：`NOT_RUN`。本次不实现接口。

后续规划：

- `POST /api/app/files/upload-url`：校验用户归属后生成短时效上传授权。
- `POST /api/app/files/{fileId}/complete`：后端确认 MinIO 对象存在并将状态改为 `READY`。
- `GET /api/app/files/{fileId}/view-url`：鉴权后生成短时效预览地址。
- `DELETE /api/app/files/{fileId}`：逻辑删除文件元数据并异步清理 MinIO 对象。
