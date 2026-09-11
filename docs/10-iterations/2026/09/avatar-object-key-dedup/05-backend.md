# 后端

`AppUser` 和 `ProfileVO` 使用 `avatarFileUrl`。`AppFileService` 在上传授权时按用户摘要复用已就绪头像；确认时从 MinIO 流式读取对象，校验实际大小、SHA-256 和 JPEG/PNG/GIF/WEBP 文件头，再只写文件 `READY` 状态。`ProfileService.updateProfile` 收到可选 `avatarFileId` 时，验证其属于当前用户、业务类型为头像、状态为 `READY` 且未删除，再在资料保存事务内写入对象 Key。删除当前对象时清空匹配的对象 Key。

对没有摘要的历史当前头像，上传授权会读取现有对象懒回填 SHA-256；命中本次图片时直接返回 `READY`，避免旧数据第一次重复上传。

所有文件查询仍通过当前 Sa-Token 用户及 `deleted=0` 限定；MinIO 调用只经过 `MinioStorageService`。
