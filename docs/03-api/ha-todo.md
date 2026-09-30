# 待办清单 API

所有接口要求 Sa-Token 登录，用户 ID 只从会话获取，不接受前端传入。与账本/记账独立。时间为北京时间的 `YYYY-MM-DDTHH:mm:ss` 墙上时间，新增/修改须晚于当前时间且不晚于 2099 年。写接口 `idempotencyKey` 须为 8–64 位英文字母、数字、下划线或短横线；相同键和相同请求可重试，同键不同请求拒绝。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/api/app/todos?status=PENDING\|COMPLETED&page=1&pageSize=20` | 分页清单，同时返回 `pendingCount`、`completedCount`、`hasMore` |
| POST | `/api/app/todos` | 新增规则和首次发生项 |
| GET | `/api/app/todos/{occurrenceId}` | 读取本人未删除的待完成/已完成发生项快照，供详情页与编辑页加载；已删除或他人记录不可读，编辑仍仅限未完成 |
| PUT | `/api/app/todos/{occurrenceId}` | 根据未完成项修改整条规则，替换未完成项（原 occurrenceId 失效，客户端保存后刷新清单） |
| POST | `/api/app/todos/{occurrenceId}/complete` | 手动完成本次发生项 |
| POST | `/api/app/todos/{occurrenceId}/delete` | 删除未完成项时结束规则；删除已完成项时只移除该历史 |
| GET | `/api/app/todos/{occurrenceId}/attempts` | 查看本人该待办最多 100 次提醒尝试，含渠道、消息标题、消息正文、状态、时间 |

新增/编辑请求：`title`（1–100 字）、`note`（最多 500 字，可空）、`dueAt`、`recurrence`（`ONCE/DAILY/MONTHLY/EVERY_N_MONTHS/YEARLY`）、`monthInterval`（每隔几月时 2–120，其余为 1）、`remind`、`idempotencyKey`。月和年的指定日期取锚定日期；目标月份没有该日时使用该月最后一天。编辑时 `dueAt` 是下一次计划时间；与当前未完成项的计划时间、重复方式和间隔一致时保留原锚定日，否则以新选择的时间重设锚定日。`remind=true` 须先在通知中心配置 Bark 或 pushplus。到期仍未完成时向当时有效的所有这两种配置发送。消息标题为「待办清单提醒」，正文首行为「哈记账： 待办标题」，次行为「计划时间：YYYY-MM-DD HH:mm」；发送记录保留同一完整正文，不回显 Key。完成和删除请求体只含 `idempotencyKey`。

重复规则会逐次生成发生项；列表计数是当前已生成且未删除的发生项数，并非无限未来次数。服务每分钟扫描，生成未来 24 小时内的发生项；手动完成后确保至少生成下一项。只对到期不足 24 小时且仍未完成的发生项建立提醒，每个渠道最多重试 5 次；失败有退避。网络超时可能由外部平台受理后产生重复，`ACCEPTED` 不等于终端送达。

## 自定义重复（V11，2026-09-30）

新增 `recurrence=CUSTOM`，旧五类请求继续兼容；新编辑界面只提供不重复开关和直接展示的三模式表单，旧记录回填为对应自定义规则。

| 字段 | 契约 |
| --- | --- |
| repeatMode | TIME按时间、AFTER_COMPLETION完成后、FIXED_DATES固定日 |
| repeatUnit / repeatInterval | 前两模式DAY/WEEK/MONTH/YEAR与1–365整数；固定日省略 |
| weekDays | TIME+WEEK必填；CSV，1周一至7周日，多选 |
| monthDays / lastDay | TIME+MONTH，CSV 1–31与月末布尔，至少选一项 |
| yearDays | TIME+YEAR必填；CSV MM-dd，公历，可跨月多选，最多366项 |
| fixedDates | FIXED_DATES必填；CSV yyyy-MM-dd，2000–2099年，最多100项 |

所有日期须真实存在（年度02-29按2000年校验）；CSV拒绝重复值。模式不相关的选择必须省略/为空，前端提交前清理隐藏字段。CUSTOM的monthInterval固定1。详情、清单、完成历史返回完整字段快照。

TIME以dueAt所在日/周（周一开始）/月/年为周期起点，在活跃周期执行所有选择；首次发生项取不早于dueAt的第一个匹配时间。月内不存在的日和非闰年的02-29取月末，同一天合并一次。TIME及FIXED_DATES保留定时生成未来24小时和完成时确保下一项的机制。固定日按所选日期排序，沿用dueAt的时刻，早于起点的日期跳过，耗尽后不再生成。所有生成时间上限2099年。

AFTER_COMPLETION只创建一个未完成项；定时扫描不提前生成。手动完成以北京时间的实际完成时间（含毫秒）加间隔创建下一项；月/年加法缺少对应日期时取月末。完成、下一项、请求幂等在同一事务，重复请求不重复生成。重复规则字段变化会重设周期锚点，仅改标题/备注且时间与规则不变时保留锚点。
