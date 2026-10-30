# 后端

lottery独立领域；LotteryStore承载类型注册、MyBatis访问、锁和聚合SQL，主表继承BaseAuditEntity，MP统一填充审计；自动操作显式SYSTEM_USER_ID。

LotteryOfficialClient固定HTTPS官方来源，校验号码/期号/日期和金额，SSQ回填不使用限制总数的issueCount=30；跨页连续链接取相邻官方分页并校验，不推算未来。LotterySyncService持久化补漏游标、历史循环复查游标和随机令牌租约。官方有效数据变化递增版本、保留原始响应修订。

LotteryRules按当期版本判断奖级，再用当期官方单注金额计算。DLT早于19019的早期规则显式待核实，不套新规则。SSQ福运奖沿连续官方记录及上一期开奖后奖池阈值传播，未知保持UNKNOWN。派奖无法确认时REVIEW。

LotteryProcessor按用户事务绑定逐期号码、结算、创建结果/到期/更正事件。单次提醒与定投到期分开，奖金未知不阻塞批次到期。LotteryDeliveryService提交后HTTP投递；未知结果保留锁、人工确认重试可能重复。删除不能撤回已发送的外部请求。

TodoNotificationSender新增传group的重载，既有4参数调用仍固定待办分组，不改待办语义。

任务开关lottery.worker.enabled（默认true）；同步间隔lottery.sync.delay-ms（默认300000），初始化延迟lottery.sync.initial-delay-ms（默认20000），结算/投递每60秒。
