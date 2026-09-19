# 回滚

回滚 `AppFileService` 的新 Key 生成逻辑并恢复旧格式即可；同步恢复本迭代更新的 API/数据库说明。无需数据库回滚或 MinIO 批量重命名。回滚后既有新旧对象引用仍按各自存储的 `object_key` 访问。
