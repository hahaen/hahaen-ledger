# 待办提醒正文前缀（2026-09-30）

- requirement：用户要求提醒内容前加「哈记账： 」。
- design：Bark/pushplus 正文统一为 `哈记账： 待办标题`，换行后保留计划时间；消息标题为「待办清单提醒」。
- database：无 Schema/Flyway 变更；发送尝试继续保存与实际发送一致的完整正文。
- api：无接口字段变更，更新当前待办契约的消息格式说明。
- backend：TodoReminderWorker 生成正文时增加前缀。
- frontend：无变更。
- testing：双渠道回归校验实际 sender 参数与审计正文完全一致，执行待办相关测试与 Java 25 打包。
- commands：实施前工作区干净，main 与 origin/main ahead/behind 均为 0；Java 25 执行 `mvn -q -Dtest=TodoReminderWorkerTest,TodoScheduleTest,TodoRepeatTest,NotificationKeyCipherTest test` 退出 0，14 项无失败/错误/跳过；`mvn -q -DskipTests package` 退出 0；`git diff --check` 退出 0。
- verification：双渠道真实 worker 调用路径及审计正文一致性单测、待办调度/重复/加密回归和打包 PASS。前端构建不适用（无前端变更）。本地通知诊断已创建一条用户授权的单次待办，pushplus 平台受理 PASS；Bark 请求 FAIL，注册查询 HTTP 400 表明保存值无法映射设备 Token，用户随后明确确认误填 Device Token；手机送达 NOT_RUN；新前缀真实终端验收 NOT_RUN（补充验收），9898 现有 JVM 未重启，加载新代码需重启本地后端。该诊断在前缀修改前执行，不作为新前缀的终端验收证据。
- rollback：revert 本次代码与文档提交；测试待办保留供用户查看，没有 Schema 回退。

## 同步与交付

已安全 stash 本次 8 个文件（含新迭代），`git pull --ff-only` 返回 Already up to date；apply 后逐文件 SHA-256 与备份一致，确认后删除 stash。同步后同一 Java 25 定向测试再次退出 0（14 项）；新增迭代链接 4 处有效；`git diff --check` 退出 0。提交前逐文件暂存并核对范围与敏感信息。

2026-10-01 追加：经用户授权，DEV Bark 配置改为推送 Key，加密保存回读一致，官方注册查询 HTTP 200（PASS）。凭证不记录于文档或命令；保留旧失败记录，仅提前该测试待办下一次 Bark 重试时间，不修改尝试次数或 pushplus 已发送状态。

最终运行核对：2026-10-01 00:01:23 Bark 第五次尝试 ACCEPTED，投递 SENT；pushplus 第一次尝试 ACCEPTED、SENT，双渠道平台受理 PASS。该 JVM 仍运行前缀修改前代码，未宣称新前缀已在终端生效。工作区只包含本次 8 个文件；临时探针位于 /tmp，未包含真实凭证或提交到仓库。
