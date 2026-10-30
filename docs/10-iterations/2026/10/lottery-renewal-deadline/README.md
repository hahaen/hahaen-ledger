# 彩票续买提醒增加下期购买截止时间

2026-10-30。开始时工作区/暂存区干净，main跟踪origin/main。

## requirement

续买提醒增加购买下期的截止日期、周几和时间。

## design

在批次汇总后追加“下期购买截止（预计）：YYYY-MM-DD（周X）HH:mm（北京时间）”。以本批末期官方开奖日期为锚点，按DLT周一三六/21:00、SSQ周二四日/20:00推算下一期；如已缓存官方相邻下一期，使用其真实开奖日期。估算注明休市、延期以官方公告及网点实际销售为准。历史批次不跳到发送日之后的新一期，截止已过时标明“已过截止时间”；无日期则明确待核实。已受理消息保持快照，不重复推送。

规则依据：[体彩止售调整公告](https://m.lottery.gov.cn/xxgk/tzggz/20221220/10027143.html)、[体彩规则](https://m.lottery.gov.cn/ksjz/m/yxgz_dlt/)、[深圳福彩销售截止问答](https://www.szlottery.org/attachment/0/63/63992/757078.pdf)、[湖北福彩开奖时间](https://www.hbfcw.cn/csyw/cszx/202609/t20260901_406057.shtml)。2026-10-30查阅；当前接口不含未来休市/销售日历，不把常规推算标为官方确认。

## database / api

无Schema、Migration、SQL、配置或API字段变化；复用共享官方开奖表只读数据，不改个人购买数据及事件幂等键。

## backend / frontend

仅后端提醒正文及对应测试受影响。H5/微信共用后端通知，不改前端源码，无需客户端更新或前端构建；微信工具/真机NOT_RUN（按用户阶段规则排除，非必需）。

## testing / commands / verification

待完成：先Review本次代码，再Java25测试/构建、真实DEV结算及事件正文验证；不向真实通知渠道发送验收消息。验证后安全保存、拉取upstream、复审复验、提交推送并自动发布后端。

## rollback

如需撤回，以新提交恢复正文及辅助逻辑，重新验证并发布后端；不回退数据库，不删除现有事件或发送历史，不自动回滚或备份旧应用。

首轮Review PASS：范围仅正文接线、日期辅助类、6项边界单测及既有DEV综合场景增加的正文/投递断言。核对两彩种销售截止与开奖时间区分、七个星期/跨年、官方相邻期日期优先、历史截止和未知日期回退；共享开奖读取不引入个人越权入口，无金额/事务/权限/幂等/客户端协议改变。修正DEV断言避免依赖官方缓存是否已有下一期，使用格式及实际投递正文核验；已受理事件仍保留原快照。无未解决问题，进入验证。

已执行验证PASS：Java25 `mvn -B -q -f server/pom.xml clean verify -Dlottery.dev.verify=true`退出0。真实DEV MySQL/Redis/MinIO可用，综合HTTP场景验证到期事件正文日期/星期/21:00、Bark发送参数完整保留正文、重复处理不新增事件、已受理快照不改写；既有跨用户权限/购买/暂停/删除回归通过。新增6项单测验证两彩种/七个星期、跨年、官方相邻日期覆盖、截止边界/延迟处理、未知锚点。渠道为测试替身，无额外真实推送。构建JAR成功；日志仅本机/tmp/ledger-renewal-deadline-backend.log。文档相对链接和git diff --check通过。前端源码/类型/协议未变，不适用前端构建或UI验收。

完整后端报告：122总/114执行通过/8条件跳过，0失败错误；其中DEV综合类8总/7执行通过/1跳过（UI工具验收未启用），新截止单测6/6通过。

同步后Review PASS：10项任务文件安全stash（含新增文件），git pull --ff-only返回Already up to date；恢复后逐文件SHA256一致且无未合并文件，确认后删除stash备份。复审正文接线/辅助逻辑/测试：读取限定彩种和官方相邻期，金额与用户权限路径不变；日期按北京时间、不以当前日跳期、无官方未来日历则标预计，已受理事件不重发。无远端源码/依赖变化，无未解决问题；进入受影响单元及DEV HTTP复验。

同步后复验PASS：Java25 `mvn -B -q -f server/pom.xml test -Dlottery.dev.verify=true -Dtest=LotteryRenewalDeadlineTest,LotteryDevIntegrationTest#actualHttpPermissionsPurchasesSettlementNotificationsAndDeletion`退出0，7/7通过（6单测+1真实DEV综合HTTP场景），0失败错误。日志/tmp/ledger-renewal-deadline-recheck.log。源码未变，前一完整clean verify构建仍对应本轮源码；发布入口再次执行clean verify。无未解决必需FAIL/BLOCKED。

发布闭环PASS：`9127d66 feat(lottery): 在续买提醒中增加下期截止日期和星期`已普通推送GitHub origin/main并核对等于本地HEAD。`python3 deploy/local/release.py backend`退出0，2026-10-30 23:44:21+08:00发布；后端分组件记录与容器OCI revision均为9127d662da0ccb5572653ab69f913845580092a3，running/0重启。发布构建122总/107执行通过/15条件跳过，0失败错误；发布前完整DEV验证114通过。服务器/app/app.jar与本地产物SHA256均为008ec9ca35cf17af47cc1a23227d98c4924a54c13d84bc06c3ea29308b4123cf。公开彩票API未登录HTTP401/code1；DEV真实事件正文及渠道发送参数验证通过，不发送额外真实通知、不宣称设备送达。

H5无需更新/未发布，保留314a217，公开静态版本5ed2d703-1045-4c59-b101-6ed1bc22b6a0；微信小程序无需更新/未发布，工具/真机NOT_RUN（按用户阶段规则排除，非必需）。数据库无SQL/Migration/配置操作，V12材料摘要保持2befcc2a90f9e19909e89f55cc1f54035c1726664ce810b7aec1cb5d38235980。服务器仅余本轮后端镜像，未遗留ledger-upload或haji.incoming目录，原数据、日志、密钥及备份保留，不备份旧应用、不自动回滚。无未解决必需FAIL/BLOCKED；后续仅文档归档无需再次发布服务或客户端。

发布命令日志/tmp/ledger-renewal-deadline-release.log。上线检查使用SSH读取容器限定状态/版本/JAR摘要、分组件记录和残留目录；公开接口及版本文件用curl系统证书校验成功。本机Python urllib首次公开检查因本机CA链缺失失败，未关闭TLS校验，改curl后复验通过；不将该失败宣称为站点故障。文档归档Review仅本次新增证据，无凭证或真实用户数据；内容/链接/差异检查后同步提交。
