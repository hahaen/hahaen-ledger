# 验证

## Review

本次未提交新增及修改代码，Standards/Spec两轴独立Review。首轮修复旧结算回写、分页断链、未知发送编辑锁、异常分类、取消事件恢复、早期DLT规则兜底、未来位置编辑及历史复查。复审再修复历史UNKNOWN阻塞官方更正和空游标更新；两轴最终静态复审通过，允许进入测试。

首轮测试：前端相关24项PASS。Java相关24项中1项FAIL（旧规则测试夹具缺九等奖金额，正确触发待核实），类型检查FAIL（uni switch事件类型）；已补齐夹具并用明确Event交叉类型读取uni detail，无any或关闭检查。对该测试/类型差异再次人工Review通过。后续验证待执行。

第二轮真实DEV：V12结构/默认值/Entity一致性、真实HTTP购买权限/兑奖/删除及UNKNOWN回归通过；官方接口因返回text/json媒体类型失败。修正为按字符串读取后严格JSON解析，仍校验业务成功/号码/期号，未放宽HTTPS；同时修正小程序text内嵌view与北京时间日期工具。以上差异人工Review通过，复验待执行。

真实H5 Chrome验收：入口位于待办下方且其余5项导航不变；单次购买必填/实际购买确认校验、成功保存、官方结果即时结算、删除确认与成功返回已执行。首页/编辑/20期批次/SSQ历史列表320/375/414px均无document横向溢出；计划、官方奖金详情已核对。截图位于evidence/；控制台仅见浏览器扩展自身clipboard脚本错误，未见彩票业务错误。截图均为隔离合成账号，金额并非真实用户中奖。

后续代码复审：AFTER_COMMIT独立事务即时兑奖；显式用户/系统更新审计；同步进度CAS与页事务行锁；官方重复金额更正按drawVersion去重；投递按毫秒租约围栏收尾，旧发送者不覆盖新状态。两轴相关复审均通过，新增并发回归待执行。

微信工具：应用已启动，但cua原生连接两次30秒超时，模拟器运行验证目前BLOCKED；已请求用户检查窗口/登录/授权状态。小程序构建PASS不替代模拟器验证。真实渠道受理验收等待指定接收账号，不使用状态机替身冒充真实平台受理。

## 首阶段汇总（已被下方范围更新取代）

PASS：Java25完整clean verify，107项中100执行通过、7条件跳过；彩票真实DEV回归5执行通过；相关前端24项、类型/H5/微信构建；真实H5静态资源与真实DEV链路、320/375/414px、即时结算及删除交互。

PARTIAL：彩票整体交付，尚未满足全部平台与真实渠道验收。DLT早期19019以前规则和未知派奖按设计显示待核实，不误算；历史官方缓存渐进回填，不能将未完成回填宣称全历史齐全。

BLOCKED：微信模拟器原生控制连接超时；真实提醒缺指定接收账号。按用户明确门槛，本轮暂不提交/推送/发布；后端/H5/微信均需更新但尚未发布。

NOT_RUN：生产V12正式迁移及摘要核对、后端/H5发布验收、用户微信上传/提审/发布；微信真机为补充未验项。

工作区初始干净，本轮改动均为彩票功能及必要共享身份/通知适配、测试和文档。git fetch后HEAD与origin/main均ee5752a，未制造空提交或推送旧提交。

补充正向到期验收PASS：真实DEV单期购买返回前自动兑奖并创建一个EXPIRY事件；重复扫描不新增事件；暂停时发送器无调用，恢复后渠道替身明确受理；plan.awaitingRenewal=true；删除清理个人记录。对应定向测试1项执行通过、0失败。真实平台受理仍未验，不混淆状态机替身结果。

最后仅文档补证与链接检查：PASS，git diff --check无误。所有本次代码已Review并完成适用的可执行验证，未将外部BLOCKED项写成PASS。


## 2026-10-30｜用户验收范围更新及真实推送

用户本轮明确“微信小程序不验证，只验证h5”，因此微信模拟器/真机运行验证从本轮必需门槛排除，历史两次连接超时记录保留，不再阻塞交付；已完成的小程序构建不等同运行验证，上传发布仍由用户负责。

PASS：DEV仅admin账号配置了Bark/pushplus。按用户“现在”的回复及已告知的接收对象，显式运行LotteryLiveNotificationDevTest（lottery.live.verify=true、lottery.live.account=admin）；两渠道各发送一条“彩票功能验收”并均返回ACCEPTED，前后lottery_enabled未变。测试隔离TodoReminderWorker，LotteryWorker关闭，未触发无关提醒。仅证明渠道受理，不宣称设备送达。

新增实际命令：Java25 mvn -f server/pom.xml -Dtest=LotteryLiveNotificationDevTest -Dlottery.live.verify=true -Dlottery.live.account=admin test，退出0。测试Key仅在内存解密，不进入命令或文档。

本轮必需功能验证与H5验收已通过，无剩余必需FAIL/BLOCKED。后续按既定流程安全保存、pull --ff-only、恢复核对、同步后复审复验，再提交推送，先正式生产V12迁移，再发布后端/H5。此处尚不宣称提交/发布已完成。


## 同步后复审与复验

PASS：stash 802a1e433845abd995d96c52c58aca29cb364e65安全保存全部本次文件，git pull --ff-only返回Already up to date；apply后94文件SHA256 manifest完全相同，确认后才删除该stash。无冲突、无远端相关上下文变化。Standards同步后复审通过，新增真实推送测试已隔离无关定时任务。

PASS：Java25 clean verify -Dlottery.dev.verify=true退出0，108总/100执行通过/8条件跳过（新增真实发送默认必须显式启用，避免发布构建重复推送）；前端相关24项、类型检查及H5构建退出0。此前真实渠道显式验收两项均受理。用户已排除微信运行验证，无剩余必需前置FAIL/BLOCKED。

生产迁移审核材料见11-production-migration.md，生产实际迁移和发布尚未执行，后续单独记录。
