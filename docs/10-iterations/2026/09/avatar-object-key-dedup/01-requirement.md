# 需求

`app_user.avatar_file_id` 改为 `avatar_file_url`，但只能保存不随 MinIO 域名变化的对象 Key。个人中心 H5 头像需要上传至 MinIO，校验文件；相同图片不得重复上传，应直接使用既有对象。
