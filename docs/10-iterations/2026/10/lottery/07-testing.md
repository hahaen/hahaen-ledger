# 测试计划

必须先完成本次代码Review后测试。新增LotteryRulesTest覆盖号码、金额、规则切换、未知奖金/福运奖；LotteryDevIntegrationTest通过显式-Dlottery.dev.verify=true启用真实DEV数据库、Redis、HTTP、官方接口与持久化验证，使用合成账号清理个人数据。该测试的通知sender为状态机替身，不算真实平台受理证据。

app/tests/lottery.test.mjs覆盖表单校验、保存防重重试和放弃编辑。后续执行类型检查、H5/微信构建、真实H5及微信开发者工具适配和操作。

执行结果：Java25完整verify共107项，100执行通过、7条件跳过；其中LotteryRulesTest 5项，LotteryDevIntegrationTest 5项执行通过、1项独立UI条件跳过。真实H5条件测试已另行运行通过。前端相关回归24项、类型检查和H5/微信构建通过。

新增真实DEV回归覆盖同步旧租约CAS、旧投递者不能覆盖新受理、UNKNOWN重试不解锁、官方A→B→A→B更正、取消事件恢复及未来位置编辑。补充单期到期/暂停恢复的正向验收结果见verification。

首阶段曾BLOCKED：微信连接超时、未指定推送接收账号。当前状态：用户取消微信运行验证；Bark及pushplus已通过显式真实发送验收（非替身），均ACCEPTED且账号权限不变。微信真机未执行且已从本轮必需范围排除。
