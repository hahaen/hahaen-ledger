# design

- V1 直接创建 `app_user.avatar_file_url`，不再创建 `avatar_file_id` 或头像外键。
- V2 直接创建头像摘要复用索引和当前文件关联结构；测试数据库没有旧数据，因此不需要历史数据回填。
- V3 直接创建 `sort_order` 和顺序查询索引；测试数据库没有旧账户，因此不需要历史数据回填。
- V1–V4 的建表语句显式声明 `utf8mb4_general_ci`；JDBC URL 显式声明连接字符集和排序规则。
