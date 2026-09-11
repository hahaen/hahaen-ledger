# 后端

无后端代码变更。既有 `ProfileService` 继续在资料保存事务中校验待关联头像属于当前用户、类型为 `AVATAR`、状态为 READY 且未逻辑删除后，再写入稳定对象 Key。
