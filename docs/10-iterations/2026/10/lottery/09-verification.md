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


## 发布后权限边界补强

最终复核发现：普通资料保存使用已加载AppUser做updateById，维护者并发关闭权限后，旧实体可能回写旧lottery_enabled值。修复为该字段MyBatis insertStrategy=NEVER、updateStrategy=NEVER，业务创建统一依赖数据库DEFAULT0，业务更新完全排除该列；维护者显式SQL配置不受影响。DTO和前端只读保持。补充真实DEV覆盖显式置1创建仍为0、旧实体不能覆盖关闭、普通Mapper强行置1仍为0。该补强无需新Migration，只影响后端；按同一流程Review、验证、同步、提交和后端发布。当前验证待执行。

权限补强首轮Review PASS；Java25 clean verify -Dlottery.dev.verify=true退出0：109总/101执行通过/8条件跳过，真实数据库只读权限回归通过，无新Migration。

权限补强同步后复审/复验PASS：stash 99bcc35e225eaed8c3ea759469006e2fdb283f1f → pull --ff-only（Already up to date）→ apply后4文件摘要完全相同，确认后删除stash；无新上下文变化，复审无未解决问题。定向ordinaryUserWritesCannotGrantOrRestoreLotteryPermission及ProfileServiceTest退出0。本轮仅后端及测试/文档，H5无改动、无新Migration。
