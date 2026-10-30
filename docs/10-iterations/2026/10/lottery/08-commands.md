# 实际命令及结果（2026-10-30）

不记录凭证、通知Key、会话和外部敏感原始响应。

|命令/操作|实际结果|
|---|---|
|git status --short / git branch -vv|初始工作区干净，main跟踪origin/main，HEAD ee5752a|
|官方两个接口curl及Java客户端分页请求|HTTP200，DLT/SSQ均30条；SSQ移除issueCount后可分页历史|
|Java25相关单测首轮|FAIL：旧规则夹具缺金额，修正后PASS|
|pnpm run typecheck首轮|FAIL：uni switch事件类型，显式类型修正后PASS|
|node --test tests/lottery.test.mjs tests/mine-profile-refresh.test.mjs tests/todo-repeat.test.mjs tests/todo-list-detail.test.mjs|24项PASS|
|pnpm run typecheck / build:h5 / build:mp-weixin|退出0，PASS；后续前端改动已复跑对应检查构建|
|mvn -f server/pom.xml -Dtest=LotteryDevIntegrationTest -Dlottery.dev.verify=true test首轮|FAIL：后台无Sa-Token上下文，修复CurrentUser可选身份并复审|
|官方Java接入第二轮|FAIL：上游text/json不支持直接JsonNode转换，改字节读取后严格JSON解析；复审并复验PASS|
|mvn -f server/pom.xml -Dtest=LotteryRulesTest,LotteryDevIntegrationTest,ProfileServiceTest,TodoReminderWorkerTest -Dlottery.dev.verify=true -Dlottery.ui.verify=true test|退出0，真实DEV＋临时隔离H5代理，手动页面验收后发送完成信号，finally清理账号|
|mvn -f server/pom.xml clean verify -Dlottery.dev.verify=true（JAVA_HOME=本机Java25）|退出0；107项中100执行PASS，7条件跳过；已生成JAR|
|mvn -f server/pom.xml -Dtest=LotteryDevIntegrationTest -Dlottery.dev.verify=true test（合成孤立账号清理后）|退出0，5执行PASS，1独立UI条件跳过|
|mvn -f server/pom.xml -Dtest=LotteryDevIntegrationTest#actualHttpPermissionsPurchasesSettlementNotificationsAndDeletion -Dlottery.dev.verify=true test|退出0，1项真实DEV回归PASS，新增单期到期、暂停不推送、恢复受理与事件去重断言|
|Chrome真实H5 320/375/414px、单次保存即时结算、删除、计划/20期批次、SSQ开奖详情|PASS，证据在evidence/；均为隔离合成账号|
|微信开发者工具cua连接|BLOCKED：应用已启动，原生连接两次30秒超时|
|ssh hahaen-server容器/迁移摘要存在性只读检查|PASS：生产原容器在运行，迁移摘要存在；未操作生产数据/发布|
|git fetch origin / git rev-parse HEAD origin/main|退出0，两者均ee5752a5d160bec39054ffe521a0c92dc1bcb9db|
|git diff --check|PASS|

首阶段因微信/真实渠道BLOCKED而暂缓提交发布；该阶段结论已被下方用户范围更新及真实推送PASS取代。生产迁移和发布状态以最新记录为准。


## 2026-10-30｜用户验收范围更新及真实推送

用户本轮明确“微信小程序不验证，只验证h5”，因此微信模拟器/真机运行验证从本轮必需门槛排除，历史两次连接超时记录保留，不再阻塞交付；已完成的小程序构建不等同运行验证，上传发布仍由用户负责。

PASS：DEV仅admin账号配置了Bark/pushplus。按用户“现在”的回复及已告知的接收对象，显式运行LotteryLiveNotificationDevTest（lottery.live.verify=true、lottery.live.account=admin）；两渠道各发送一条“彩票功能验收”并均返回ACCEPTED，前后lottery_enabled未变。测试隔离TodoReminderWorker，LotteryWorker关闭，未触发无关提醒。仅证明渠道受理，不宣称设备送达。

新增实际命令：Java25 mvn -f server/pom.xml -Dtest=LotteryLiveNotificationDevTest -Dlottery.live.verify=true -Dlottery.live.account=admin test，退出0。测试Key仅在内存解密，不进入命令或文档。

本轮必需功能验证与H5验收已通过，无剩余必需FAIL/BLOCKED。后续按既定流程安全保存、pull --ff-only、恢复核对、同步后复审复验，再提交推送，先正式生产V12迁移，再发布后端/H5。此处尚不宣称提交/发布已完成。


## 同步后复审与复验

PASS：stash 802a1e433845abd995d96c52c58aca29cb364e65安全保存全部本次文件，git pull --ff-only返回Already up to date；apply后94文件SHA256 manifest完全相同，确认后才删除该stash。无冲突、无远端相关上下文变化。Standards同步后复审通过，新增真实推送测试已隔离无关定时任务。

PASS：Java25 clean verify -Dlottery.dev.verify=true退出0，108总/100执行通过/8条件跳过（新增真实发送默认必须显式启用，避免发布构建重复推送）；前端相关24项、类型检查及H5构建退出0。此前真实渠道显式验收两项均受理。用户已排除微信运行验证，无剩余必需前置FAIL/BLOCKED。

生产迁移审核材料见11-production-migration.md，生产实际迁移和发布尚未执行，后续单独记录。
