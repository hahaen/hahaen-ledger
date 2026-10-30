# API

所有/api/app/lottery接口从Sa-Token获取用户，实时校验有效账号和lottery_enabled=1，个人数据验证user_id。拒绝使用客户端userId及账本归属。业务拒绝沿用现有400响应，认证失效沿用401，避免前端现有403认证恢复流程误把功能权限拒绝当成会话失效。

- GET /api/app/lottery?game=DLT|SSQ&kind=single|plan&page=1：20条分页，summary、通知配置状态、同步状态。
- GET /api/app/lottery/draws?game=DLT|SSQ&page=1：本地开奖分页。
- GET /api/app/lottery/{entry|plan|batch|draw}/{id}?page=1：详情；计划批次分页；批次最多20条逐期明细。
- POST /api/app/lottery/{single|plan}：单次记录或仅创建计划。
- POST /api/app/lottery/plan/{id}/purchase：确认实际购买1–20期。
- PUT /api/app/lottery/{entry|plan}/{id}：编辑；已结算定投快照/已受理单次/发送不确定记录禁改。
- POST /api/app/lottery/{entry|plan}/{id}/delete：软删，计划级联个人批次与明细。
- POST /api/app/lottery/plan/{id}/status：ACTIVE/PAUSED/ENDED，结束不可恢复。
- POST /api/app/lottery/delivery/{id}/retry：人工确认重试失败/未知投递，未知历史不解锁购买信息。

保存参数：game、name、red、blue、multiple、additional、issue、periods、purchasedOn、note、confirmedPurchase、idempotencyKey。action参数status及idempotencyKey。

所有写接口用户行锁＋用户内幂等键：相同请求返回原结果，同键不同请求拒绝。ID对外使用字符串；金额为整数分，客户端兼容数字或数字字符串，奖金未知为null。通知Key不进入彩票响应。
