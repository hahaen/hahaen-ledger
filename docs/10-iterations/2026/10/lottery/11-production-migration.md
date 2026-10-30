# 生产V12正式迁移审核

范围：仅新增app_user.lottery_enabled（NOT NULL DEFAULT 0及0/1约束）和10张彩票表。未修改任何V1–V11，不更新实际用户权限、账本或金额数据，不做反向DDL。DEV已真实执行并验证。

生产只读预检：数据库haji，Flyway当前11且成功，彩票表0、lottery_enabled字段0；旧后端正常运行。生产与DEV数据隔离。

执行顺序（执行结果另记，不将计划标PASS）：
1. 代码提交推送并核对upstream。
2. 上传已验证的新JAR和临时编译迁移入口，不上传项目源码或凭证。新JAR内12份迁移与当前源码逐字节核对。
3. 持有既有release.lock；核对旧迁移摘要与V1–V11一致。
4. 使用MySQL官方mysqldump单事务导出haji，压缩保存服务器私有数据库备份，核对退出码和压缩完整性。只备份数据库，不备份旧应用/镜像/网页/配置。
5. 临时Java入口只从既有haji-prod.env取得连接配置；启动前检查目标catalog为haji，彩票结构不存在；Flyway validate且仅V12 pending，禁止baseline/clean/乱序，只允许11→12。
6. 使用JAR内正式SQL执行Flyway；核对版本12、10张表、9张Entity完整字段集及中文注释、权限字段默认0且真实账号全部未开通。
7. 所有检查成功后才记录新migration摘要及不含密钥的迁移收据，保持普通发布摘要门槛有效。
8. 停止并删除临时迁移容器/上传产物；保留数据库备份和迁移审计收据。随后运行release.py all。失败不自动回滚、不降级数据库，并停止发布。

迁移入口采用新JAR的PropertiesLauncher加载独立编译class，不启动Spring业务上下文或任何定时任务。本机loader smoke PASS。入口源代码SHA256：264c3f3a89e466eb098a09bed0bff9cb91fb903e27796c37d78e8ef76315e793。实际运行配置、数据库数据和备份内容均不进入Git。
