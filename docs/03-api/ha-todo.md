# 待办清单 API

所有接口要求 Sa-Token 登录，用户 ID 只从会话获取，不接受前端传入。与账本/记账独立。时间为北京时间的 `YYYY-MM-DDTHH:mm:ss` 墙上时间，新增/修改须晚于当前时间且不晚于 2099 年。写接口 `idempotencyKey` 须为 8–64 位英文字母、数字、下划线或短横线；相同键和相同请求可重试，同键不同请求拒绝。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/api/app/todos?status=PENDING\|COMPLETED&page=1&pageSize=20` | 分页清单，同时返回 `pendingCount`、`completedCount`、`hasMore` |
| POST | `/api/app/todos` | 新增规则和首次发生项 |
| GET | `/api/app/todos/{occurrenceId}` | 读取本人尚未完成的待办详情，供独立编辑页重新加载；已完成、已删除或他人的待办不可编辑 |
| PUT | `/api/app/todos/{occurrenceId}` | 根据未完成项修改整条规则，替换未完成项 |
| POST | `/api/app/todos/{occurrenceId}/complete` | 手动完成本次发生项 |
| POST | `/api/app/todos/{occurrenceId}/delete` | 删除未完成项时结束规则；删除已完成项时只移除该历史 |
| GET | `/api/app/todos/{occurrenceId}/attempts` | 查看本人该待办最多 100 次提醒尝试，含渠道、消息标题、消息正文、状态、时间 |

新增/编辑请求：`title`（1–100 字）、`note`（最多 500 字，可空）、`dueAt`、`recurrence`（`ONCE/DAILY/MONTHLY/EVERY_N_MONTHS/YEARLY`）、`monthInterval`（每隔几月时 2–120，其余为 1）、`remind`、`idempotencyKey`。月和年的指定日期取锚定日期；目标月份没有该日时使用该月最后一天。编辑时 `dueAt` 是下一次计划时间；与当前未完成项的计划时间、重复方式和间隔一致时保留原锚定日，否则以新选择的时间重设锚定日。`remind=true` 须先在通知中心配置 Bark 或 pushplus。到期仍未完成时向当时有效的所有这两种配置发送，发送记录不回显 Key。完成和删除请求体只含 `idempotencyKey`。

重复规则会逐次生成发生项；列表计数是当前已生成且未删除的发生项数，并非无限未来次数。服务每分钟扫描，生成未来 24 小时内的发生项；手动完成后确保至少生成下一项。只对到期不足 24 小时且仍未完成的发生项建立提醒，每个渠道最多重试 5 次；失败有退避。网络超时可能由外部平台受理后产生重复，`ACCEPTED` 不等于终端送达。
