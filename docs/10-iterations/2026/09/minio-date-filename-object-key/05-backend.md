# 后端

`AppFileService` 创建新头像文件时生成 `avatars/<上海时区yyyyMMdd>/<userId>/<原文件名主干>-<UUID>.<MIME扩展名>`。原名沿用既有长度和路径分隔符清理，再清理控制字符；缺少有效主干时使用 `file`。

扩展名仍由经过允许列表校验的 `contentType` 确定。摘要去重、幂等键、MinIO 代理服务、内容校验和状态转换未改变。历史对象 Key 和当前头像引用不会被覆盖。
