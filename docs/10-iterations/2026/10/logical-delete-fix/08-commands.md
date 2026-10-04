# 实际执行记录

1. 修改前核对：工作区/暂存区干净，main，upstream=origin/main。
2. Java 25 + 实际 Mapper SQL 探针（临时目录，未入库）：普通 updateById 生成 `UPDATE transaction_detail SET ... deleted_at=? ... WHERE id=? AND deleted=0`，SET 缺少 deleted，断言失败，确认根因。
3. `mvn -q -f server/pom.xml -Dtest=LogicalDeleteSqlTest test`：修改前2项ERROR，缺少显式softDeleteById映射；修复后同2项PASS，校验deleted=1、归属、未删除条件和六个审计字段。
4. `cd server && JAVA_HOME=<本机Java25> mvn -q -Ddelete.dev.verify=true package`：初次99项中1项FAIL，为旧日志路径断言（期待Windows路径、现有配置为macOS路径）；已同步断言，未改变配置。
5. 同命令最终退出0：99 tests / 0 failures / 0 errors / 4 skipped，95项执行通过；TransactionServiceTest 15、AccountServiceTest 8、SQL回归2、DEV MySQL5、日志配置1全部通过。JAR已生成。
6. DEV5项：四类账单含支出/收入关联退款，真实查询、删除审计、余额恢复、重复删除、他人账单/账户拒绝、账户列表/详情剔除、历史账单保留；零影响行数注入验证真实事务回滚。合成数据在测试事务/TransactionTemplate回滚，测试关闭通知worker，未触发通知发送。
7. 删除入口源码全仓检索完成；其余退款、物品、文件、通知配置、待办均为显式SQL，无本次字段排除缺陷。
8. `git diff --check`：PASS。

9. 含未跟踪文件stash备份后执行 `git pull --ff-only`：Already up to date。apply恢复后19个任务文件SHA-256全部一致，确认恢复成功才drop本次stash；无冲突、无既有待推送提交。
10. 同步后 Java25 执行 `mvn -q -Dtest=LogicalDeleteSqlTest,AccountServiceTest,TransactionServiceTest,LoggingProfileConfigTest test`：26项全部PASS。远端未改变源码/依赖，前述全量打包与DEV5项证据仍对应最终代码。环境连接信息、凭证与原始启动日志不写入文档。
