# 待办清单数据库（V10）

待办清单只以 `user_id` 归属当前登录用户，与账本、账户、记账流水没有关联。V10 新建 `ha_todo_rule`（重复规则）、`ha_todo_occurrence`（逐次发生项和完成历史）、`ha_todo_delivery`（每个发生项和通知渠道的重试状态）、`ha_todo_delivery_attempt`（**每次发送尝试**的渠道、标题、正文、结果、时间）和 `ha_todo_request`（用户内唯一幂等键）。五表均为 `CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci`，所有字段有中文注释。前四表是主表，继承十个公共审计字段；幂等请求是关系表，仅含 `created_at`、`deleted`。

规则、发生项分离使一天未完成不会阻塞下一次重复。发生项保存标题、备注、重复规则、首次时间及提醒开关快照，完成历史不因后续编辑而变更。删除未完成项结束规则并软删除该规则全部未完成发生项；完成历史保留。删除已完成项只软删除该记录。编辑未完成规则会软删除其全部未完成项，再创建新首次发生项，历史不变。

投递状态 `PENDING/SENDING/SENT/FAILED/SKIPPED` 由 `(occurrence_id, channel)` 唯一约束。尝试表每次发送前插入 `SENDING` 记录，完成后更新为 `ACCEPTED/FAILED/SKIPPED`；消息内容不含 Bark/pushplus Key，也不保存外部原始响应。`ACCEPTED` 仅表示外部平台接口受理，不代表设备送达。网络超时后的重试可能产生外部重复提醒。已完成或删除项在发送前再检查并跳过。所有自动创建/写入记录使用集中 `SYSTEM_USER_ID` 审计。

DEV Flyway V10 已执行；集成测试按 `information_schema` 对齐两张业务 Entity 的列集合，验证五表字符序和中文注释。V10 已在共享 DEV 执行，后续变更必须另起新 Migration。
