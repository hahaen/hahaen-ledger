# 前端

`pages/profile/profile.vue` 的 `chooseAvatar()` 在上传成功后立即写入 `avatarUrl`，使 `<image>` 展示新头像短时预览；同时保留 `pendingAvatar`，且不触发资料 PUT。提示文案说明预览已完成、点击保存后才更新。
