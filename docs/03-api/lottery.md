# 彩票 API

所有接口要求登录、有效用户及lottery_enabled=1；个人数据只能访问当前用户。权限拒绝沿用现有业务400；不会将普通功能拒绝当成会话失效。ID采用字符串；金额为整数分，客户端兼容数字或数字字符串，未知金额NULL。

|方法|接口|用途|
|---|---|---|
|GET|/api/app/lottery?game=DLT&kind=single&page=1|20条分页，kind=single/plan，含统计和同步状态|
|GET|/api/app/lottery/draws?game=SSQ&page=1|本地历史开奖|
|GET|/api/app/lottery/{kind}/{id}?page=1|kind=entry/plan/batch/draw详情|
|POST|/api/app/lottery/{kind}|kind=single/plan创建|
|PUT|/api/app/lottery/{kind}/{id}|kind=entry/plan编辑|
|POST|/api/app/lottery/plan/{id}/purchase|确认购买1–20期|
|POST|/api/app/lottery/{kind}/{id}/delete|kind=entry/plan软删|
|POST|/api/app/lottery/plan/{id}/status|ACTIVE/PAUSED/ENDED，结束不可恢复|
|POST|/api/app/lottery/delivery/{id}/retry|确认重试失败/未知投递|

保存参数和状态详见[本次契约](../10-iterations/2026/10/lottery/04-api.md)。所有写接口必须提供8–64位幂等键，相同键不同内容拒绝。用户信息GET /api/app/user/profile新增只读lotteryEnabled。

单次渠道受理后禁改；发送中/结果未知禁改，重试不消除未知历史安全锁。未开奖定投快照可修改，已结算不可改；未来期号由官方连续列表绑定。

## 定投卡片进度

kind=plan列表和计划详情新增progress：batchId（字符串/空）、startIssue、periods、drawnCount、remainingCount、estimatedReminderDate（北京时间日期/空）、reminderState。仅最新实际购买批次，不混入历史批次；累计summary含义保持。drawnCount按已有draw_id统计，与奖金是否核实无关。

reminderState为NO_PURCHASE/PAUSED/ENDED/NO_CHANNEL/ACCEPTED/DUE/FAILED/UNKNOWN/UNCONFIRMED/WAITING_SYNC/WAITING。ACCEPTED只代表至少一个配置渠道受理；预计日期不构成新的调度时间，实际仍按末期官方开奖同步后的既有任务发送。未知起始期/跨年无锚点不猜日期；预测按常规开奖日估算，休市、延期及同步延迟会调整。无数据库写入及Migration。

## 开奖列表个人汇总

GET /api/app/lottery/draws每条新增personalSummary：costCents（字符串分）、prizeCents（字符串分/NULL）、entryCount、pendingCount。按当前登录用户、彩种、期号汇总全部未删除单次与定投购买；不接受userId。无购买返回两个金额"0"及两个计数0。有任一记录未结算、奖金未知或开奖修订待重算时prizeCents=NULL，pendingCount为该类记录数，成本仍包含这些实际购买。不返回部分奖金作为总奖金。仅批量查询当前页最多20期，分页和官方详情不变。
