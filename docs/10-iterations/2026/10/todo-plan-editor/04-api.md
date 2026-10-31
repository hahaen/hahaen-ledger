# API

新增POST /api/app/todos/preview，只读返回times（1至3项）、afterCompletion、scheduleChanged；请求plan为现有计划字段，occurrenceId可选且验证本人未完成归属。无标题、通知配置、幂等要求，无数据写入。正式契约见[当前API](../../../../03-api/ha-todo.md)。

写入维持现有字段。编辑原计划不变时回传原dueAt及原recurrence/规则，允许逾期且不丢失秒/毫秒；仅内容/提醒修改保留全部发生项ID及周期，真正重排才替换。TIME允许今天作为开始日期，已过时刻取下一匹配项，其余新首项须未来。
