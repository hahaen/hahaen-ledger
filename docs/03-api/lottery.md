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
