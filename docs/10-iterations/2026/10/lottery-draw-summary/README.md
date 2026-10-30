# 开奖列表个人成本与奖金（2026-10-30）

初始工作区/暂存区干净，main跟踪origin/main。

## requirement

大乐透、双色球开奖卡片右下角显示当前用户该期已登记实际购买的成本与税前奖金，包括单次和定投；保留进入官方奖金依据详情的入口。

## design / frontend

沿用白色卡片、12px辅助文字和MoneyDisplay，底部左侧查看入口，右侧两行成本/奖金；长金额和窄屏允许换行。无购买为0，奖金未完成核实为“待确认”，接口缺字段时显示“暂不可用”，不伪造零值。H5/微信共享模板。

## database / api / backend

无Schema、Migration、配置或持久化写入变化。draws列表每条新增personalSummary（costCents字符串分、prizeCents字符串分或NULL、entryCount、pendingCount）。仅查询当前页最多20期，按当前登录用户/彩种/期号汇总且deleted=0；不关联账本。奖金仅在全部记录SETTLED、金额非NULL且结算版本匹配当前开奖时输出总数，否则NULL。不修改开奖详情或共享官方表。数据访问集中LotteryStore，一次批量聚合，避免逐卡查询。

## testing / commands / verification

计划：本次差异Review后执行Java25单元与真实DEV HTTP金额/隔离/删除验证、前端回归/类型/H5及微信命令行构建、Chrome后台实际H5画面检查。微信工具/真机和发布NOT_RUN（按用户阶段规则排除，非必需）。实际结果后补。

## rollback

如需撤销，新增正向提交撤回只读聚合/展示并按流程重新发布后端/H5；无需数据库回退，不备份旧应用、不自动回滚。

## 首轮 Review

PASS：仅审查本次LotteryStore批量聚合、LotteryService列表拼装、共享TS/模板/4条样式及相关测试。核对当前身份、功能开关、两彩种、逻辑删除0/NULL排除、单次/定投合计、整数分字符串、未知与未中奖区分、修订版本、当前页最多20期及参数绑定。未新增业务写入、事务、缓存或文件访问。发现通用MoneyDisplay的nowrap会影响极长金额，已在本区域覆盖为normal并复审；无未解决问题，可以开始测试。

首轮验证发现两处测试代码问题：HTTP辅助方法漏声明受检异常、pnpm严格依赖布局下测试直接引用Vue内部包不可解析。已修正异常声明，改用Vue官方包导出compiler-sfc/server-renderer；对以上测试差异复审通过，再运行验证。业务实现未变。

## 已执行验证

PASS：前端 `cd app && node --test tests/*.test.mjs` 200/200；新增两个彩种实际SFC渲染检查，包括金额格式、0、待确认、旧接口回退及分页入口。`pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin`均退出0，`node tests/wechat-share-build.check.mjs` 25/25。命令行构建未调用微信工具。

PASS：Chrome实际H5构建、本地只读示例及CSP同源限制下验证两彩种320×740、375×812、414×896、812×375页面及卡片无横向溢出；覆盖已购/无购买/待确认/长金额/旧接口缺字段，详情导航和加载更多正常。页面改彩种后完整重载再核对期号，避免仅改hash沿用页面旧状态。截图：[大乐透375px](evidence/h5-dlt-375.png)、[双色球375px](evidence/h5-ssq-375.png)。本地夹具未连接真实业务后端、拒绝POST，不能视为真实金额结算验收。控制台仅见第三方Chrome扩展脚本错误，无本应用错误。

日志位于 `/tmp/ledger-draw-summary-{backend,frontend,type,h5,mp}.log`，只读预览脚本 `/tmp/ledger-draw-summary-preview.py` 不提交。不新增SQL/Migration/配置；微信工具/真机及发布NOT_RUN（按用户阶段规则排除，非必需）。后端真实DEV验证仍在进行，完成后记录。

PASS：Java25 `mvn -B -q -f server/pom.xml clean verify -Dlottery.dev.verify=true`退出0。116总/108执行通过/8条件跳过，0失败错误。新增真实DEV HTTP场景对DLT/SSQ验证单次+定投800分成本/1700分奖金、另一用户9800分隔离、无购买0、deleted=1与NULL排除、彩种隔离、待核实NULL、官方修订旧奖金NULL、已核实未中奖0、删除后0及撤权拒绝；MySQL/Redis/MinIO探测成功。仅合成验收账号/记录写入DEV并finally清理，未改变实际用户数据。无必需FAIL/BLOCKED，条件跳过包括微信/浏览器专用或额外现场通知验收，不将其声称为已执行。

同步前复审PASS：实现和金额/权限查询仍为本次范围，没有未解决问题；全部必需验证已通过。后续先安全保存，再同步upstream及复审复验。

## 同步后复审

PASS：17个任务文件安全stash后执行`git pull --ff-only`，远端Already up to date；stash apply成功，所有文件SHA-256与保存前逐字节相同，确认恢复后删除stash。无冲突、无无关差异。仅本轮API查询/金额/归属/修订待核实/双端模板/样式/测试再次Review，无未解决问题，开始同步后受影响复验。构建输入未变，双端既有构建对应同步后源码。

同步后复验PASS：真实DEV新HTTP综合场景退出0（两彩种金额/隔离/删除/未知/修订/撤权），前端200/200、TypeScript、25页微信分享产物复验通过。源码/依赖未变，沿用相同输入的H5/微信构建；正式发布入口再构建后端/H5。文档相对链接、`git diff --check`通过，待提交推送后执行`python3 deploy/local/release.py all`。

## 发布闭环

PASS：`b6106ed feat(lottery): 在开奖卡片展示个人成本与奖金`普通推送GitHub origin/main，`git ls-remote origin refs/heads/main`与本地HEAD一致。`python3 deploy/local/release.py all`退出0，后端及H5均于2026-10-30 23:30:26+08:00发布，分组件记录及容器OCI revision均为b6106edb41ee7ad6cec2651d879a0947ef7f9749；容器running/0重启。发布构建116总/101执行通过/15条件跳过、0失败错误；发布前含真实DEV完整验证108执行通过。

PASS：公开HTML、7个静态资源（含开奖页JS/CSS入口及共享lottery模块）、haji-version.json与本地发布产物逐字节一致，静态版本0b601e07-13e7-4f59-b4a2-6e160ac4515c；公开未登录开奖API返回401。Chrome独立后台登录会话实页确认大乐透最新期个人成本/奖金显示2/0，既有中奖期显示2/5，未购买期0/0；双色球最新期0/0。线上大乐透375px无横向溢出，双色球默认视口读取正常；两彩种四尺寸已在本地构建验收。只读未新增或修改实际用户购买记录，临时页面、视口覆写及本地预览服务器均已清理。线上截图只保留本机/tmp，不提交真实用户金额画面。

后端需要更新且已发布成功；H5需要更新且已发布成功；微信共享源码、类型及命令行构建已同步，使用新界面需用户自行上传发布，工具/真机及发布NOT_RUN（按用户阶段规则排除，非必需）。数据库无SQL/Migration/配置操作；数据、密钥、日志、备份保留。服务器仅保留当前后端镜像，无ledger-upload或haji.incoming残留目录；不备份旧版本、不自动回滚。无未解决必需FAIL/BLOCKED；后续仅文档归档无需重复发布服务或客户端。
