# 待办清单 API

所有接口要求 Sa-Token 登录，用户 ID 只从会话获取，不接受前端传入。与账本/记账独立。时间为北京时间的 `YYYY-MM-DDTHH:mm:ss` 墙上时间，新建/重新安排的实际首项须晚于当前时间且不晚于2099年；按时间模式开始日期可为今天，已过的执行时刻跳到下一匹配项。编辑原计划完全不变时允许保留逾期时间。写接口 `idempotencyKey` 须为 8–64 位英文字母、数字、下划线或短横线；相同键和相同请求可重试，同键不同请求拒绝。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/api/app/todos?status=PENDING\|COMPLETED&page=1&pageSize=20` | 分页清单，同时返回 `pendingCount`、`completedCount`、`hasMore` |
| POST | `/api/app/todos` | 新增规则和首次发生项 |
| POST | `/api/app/todos/preview` | 只读计划预览；返回最多3项实际时间，不创建数据 |
| GET | `/api/app/todos/{occurrenceId}` | 读取本人未删除的待完成/已完成发生项快照，供详情页与编辑页加载；已删除或他人记录不可读，编辑仍仅限未完成 |
| PUT | `/api/app/todos/{occurrenceId}` | 更新整条规则：仅内容/提醒变化保留全部未完成项及ID；时间/规则变化才替换未完成项（原ID失效） |
| POST | `/api/app/todos/{occurrenceId}/complete` | 手动完成本次发生项 |
| POST | `/api/app/todos/{occurrenceId}/delete` | 删除未完成项时结束规则；删除已完成项时只移除该历史 |
| GET | `/api/app/todos/{occurrenceId}/attempts` | 查看本人该待办最多 100 次提醒尝试，含渠道、消息标题、消息正文、状态、时间 |

新增/编辑请求：`title`（1–100 字）、`note`（最多 500 字，可空）、`dueAt`、`recurrence`（`ONCE/DAILY/MONTHLY/EVERY_N_MONTHS/YEARLY`）、`monthInterval`（每隔几月时 2–120，其余为 1）、`remind`、`idempotencyKey`。月和年的指定日期取锚定日期；目标月份没有该日时使用该月最后一天。`dueAt` 在新建/重排时是计划起点（固定日期表单由最早所选日期与执行时刻组合）；TIME以起点日/周/月/年为周期基准，匹配出的首次时间可能不同。编辑时计划完全不变须回传所选发生项原dueAt（含秒/毫秒），保留旧recurrence与重复字段；也兼容回传原anchorAt。内容/提醒修改不重建发生项，保留所有未完成项ID/dueAt/anchorAt、规则nextDueAt及已发送记录；仅新时间/规则才重排。`remind=true` 须先在通知中心配置 Bark 或 pushplus。到期仍未完成时向当时有效的所有这两种配置发送。消息标题为「待办清单提醒」，正文首行为「哈记账： 待办标题」，次行为「计划时间：YYYY-MM-DD HH:mm」；发送记录保留同一完整正文，不回显 Key。完成和删除请求体只含 `idempotencyKey`。

重复规则会逐次生成发生项；列表计数是当前已生成且未删除的发生项数，并非无限未来次数。服务每分钟扫描，生成未来 24 小时内的发生项；手动完成后确保至少生成下一项。只对到期不足 24 小时且仍未完成的发生项建立提醒，每个渠道最多重试 5 次；失败有退避。网络超时可能由外部平台受理后产生重复，`ACCEPTED` 不等于终端送达。

## 自定义重复（V11，2026-09-30）

新增 `recurrence=CUSTOM`，旧五类请求继续兼容；新增/编辑统一四模式：不重复（ONCE，新增默认）、按时间、完成后、固定日期。旧规则映射为对应表单，无计划变化时保留原协议；有意调整才转换CUSTOM。

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

## 计划预览与表单（2026-10-31）

`POST /api/app/todos/preview`请求为`{plan:{recurrence,monthInterval,dueAt,repeatMode,repeatUnit,repeatInterval,weekDays,monthDays,lastDay,yearDays,fixedDates},occurrenceId?}`。plan只含计划字段，不需要标题、通知配置或幂等键；无写入副作用。occurrenceId编辑时传入，必须为当前用户未删除未完成项，并校验规则归属和活跃状态。未登录拒绝。响应`{times:[北京时间LocalDateTime...],afterCompletion:boolean,scheduleChanged:boolean}`，times至少1项最多3项；完成后仅返回本次，不猜未来完成时间。新建scheduleChanged=false；编辑按共享计划比较逻辑返回是否重排。

预览和写入共用TodoPlanning/TodoRepeat/TodoSchedule，不复制浏览器端日期算法；2099年前无可用项返回TODO_DATE_INVALID，错误和越权不写数据。TIME起点今天但执行时刻已过时，不补生成过去项，首次取晚于现在的下一个规则匹配时间，锚点仍为用户选择的起点。固定日期新增/重排不再有隐藏开始日期，最早日期决定dueAt；编辑原计划不变仍保留历史起点及精度。
