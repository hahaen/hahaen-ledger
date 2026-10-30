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


## 最终交付（2026-10-30 13:43，北京时间）

PASS：实现提交 `ba1bed0 feat(lottery): 新增独立彩票记录与定投提醒` 已普通推送 GitHub origin/main，核对远端等于本地。按用户最新要求只验H5，不执行微信模拟器/真机运行验收或上传发布。

生产正式V12迁移PASS：迁移前haji为V11，数据库单事务压缩备份及完整性核对成功；首次入口独立validate将待执行V12判为错误，DDL前安全停止，复查仍V11/无彩票表。修正为migrate(validateOnMigrate=true)内部强制校验并经复审后重试成功，不添加ignore规则。最终V12成功、10张表、9个Entity字段集/注释、权限默认0及真实账号开通数0均核对。迁移摘要 `2befcc2a90f9e19909e89f55cc1f54035c1726664ce810b7aec1cb5d38235980` 在全部检查成功后更新，未绕过发布门槛。两份数据库备份及迁移日志/收据保留于服务器私有db-backups；未创建旧应用版本备份。

发布：`python3 deploy/local/release.py all` 退出0；先后端健康检查通过，再更新H5。

|组件|需要更新|实际结果|版本/依据|
|---|---|---|---|
|后端|是，新增API、权限、同步和兑奖任务|PASS，已发布|容器running、0重启，OCI revision=ba1bed06e7453b825553256250de3bc4b78855aa；backend-release.txt时间13:43:06+08:00|
|H5|是，新增彩票页面及入口|PASS，已发布|h5-release.txt时间13:43:07+08:00；HTML、5个彩票JS模块及5个入口资源逐字节匹配本机构建|
|微信小程序|使用新彩票入口需要新版客户端|本轮未运行验收、未上传/提审/发布|按用户指令排除；此前构建通过，后续用户自行发版|
|数据库|是，先V12再应用|PASS，正式Flyway已完成|生产V12 success=1，全部实际用户lottery_enabled=0|

运行验收PASS：生产自动同步DLT30期（最新26123，13:43:25.766成功）、SSQ30期（最新2026124，13:43:26.341成功），last_error均为空，历史回填游标2；后续后台继续增量/历史补齐。不能据此宣称全部历史已回填。彩票公开接口未登录返回HTTP401/code1。仅剩当前后端镜像，无ledger-upload/ledger-migrate临时目录。原数据库、Redis、MinIO、运行密钥、文件、日志和数据备份保留。

真实通知PASS：BARK和PUSHPLUS各一条“彩票功能验收”均ACCEPTED，既有admin权限未改变；不宣称设备送达。默认所有真实用户无入口，维护者后续按用户ID设置app_user.lottery_enabled=1才能使用。

当前无未解决必需FAIL/BLOCKED。边界：DLT早于19019及未知派奖按设计待核实；完整历史渐进回填。微信运行按用户明确指令不适用，不将未执行写成PASS。最终纯文档证据提交不触发重复发布。

发布入口Java25 verify实际统计：108总/95执行通过/13条件跳过，0失败/0错误；发布构建不重复发送真实提醒。


## 权限补强最终发布（2026-10-30 13:58，北京时间）

PASS：`6980b29 fix(lottery): 禁止业务写入回写彩票权限` 已普通推送GitHub origin/main并核对一致。`python3 deploy/local/release.py backend`退出0，仅后端更新；H5无源码变化，保持原已发布版本。

|组件|最终运行版本|实际验证|
|---|---|---|
|后端|6980b2956a35f594187327d42ae36ae1f58e8650|13:58:02+08:00发布；running/0重启，OCI revision及backend-release.txt一致，公开彩票接口401/code1|
|H5|ba1bed06e7453b825553256250de3bc4b78855aa|保持13:43:07+08:00成功发布版本，无需为纯后端补强重复更新|
|生产数据库|Flyway V12 success=1|无新增Migration；原摘要保持2befcc2a90f9e19909e89f55cc1f54035c1726664ce810b7aec1cb5d38235980，实际账号开通数0|
|微信小程序|本轮未发布|按用户要求不做运行验收，后续由用户上传新版才可使用新入口|

生产自动同步及历史保留PASS：13:58:23.856 DLT、13:58:24.982 SSQ成功，各120期本地缓存，错误为空，历史回填游标5；重启未清空缓存。仅剩当前后端镜像，上传临时目录为空；数据库、业务文件、配置、密钥、日志和数据备份保留。

权限测试PASS：创建实体即使显式给1仍落库默认0；读到旧1后维护者关闭为0，再保存资料不恢复1；普通Mapper强制赋1不生效。该字段仅维护者显式SQL配置，查询仍正常读1/0，无角色系统。

本轮最终无未解决必需FAIL/BLOCKED。后端/H5已实际发布；微信运行按用户指令排除且未发布。后续纯文档证据提交无需再发布任何服务或客户端。

权限补强发布构建统计：109总/95执行通过/14条件跳过，0失败/0错误；发布前含真实DEV的完整验证为109总/101执行通过/8条件跳过。
