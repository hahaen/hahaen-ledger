# 设计

文件上传完成后，将返回的短时 `viewUrl` 同时写入页面 `avatarUrl` 和待保存的 `pendingAvatar`。`pendingAvatar.fileId` 仅作为资料保存时的候选关联，不代表当前用户头像已经改变。

页面选择头像阶段不调用 `PUT /api/app/user/profile`；保存时继续仅在 `pendingAvatar` 存在时提交 `avatarFileId`，由既有资料事务校验并关联头像对象 Key。
