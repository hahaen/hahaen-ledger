# 开奖成本与奖金同行（2026-10-30）

初始工作区及暂存区干净，main跟踪origin/main。

## requirement

按用户补充要求，将大乐透/双色球开奖卡片的成本和奖金放在同一行，整体靠右下角。

## design / frontend

仅调整共享样式：金额摘要由纵向改为横向，成本与奖金组之间12px，标签和金额之间4px，基线对齐；窄屏允许整个摘要移到下一行并右对齐，两个指标仍横向排列。保留金额完整显示与未知状态。

## database / api / backend

无变化，不执行SQL/Migration或配置操作，后端无需更新。不修改个人金额汇总口径、接口、权限或业务写入。

## testing / commands / verification

先审查本次样式及文档，再执行前端回归、类型检查、H5/微信项目命令行构建及Chrome后台两彩种四尺寸验证。微信工具/真机及发布NOT_RUN（按用户阶段规则排除，非必需）。实际结果后补。

## rollback

如需撤回，新增正向提交还原本次样式并重新验证发布H5；无数据库回退，不备份旧应用、不自动回滚。

## Review

PASS：仅审查本次3条共享样式与对应文档。移除纵向排列、金额组不换行、标签禁止收缩；摘要可整体换行并通过margin-left:auto靠右，不改变通用lottery-inline或其他页面。不新增组件、API调用或金额计算。H5/微信均使用同一CSS规则，无未解决问题，可开始验证。本轮仅布局微调，不新增镜像实现的测试，使用既有金额渲染回归及实际浏览器几何检查。

## 已执行验证

PASS：`cd app && node --test tests/*.test.mjs` 200/200，`pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin`退出0，`node tests/wechat-share-build.check.mjs`25/25。日志`/tmp/ledger-draw-inline-{front,type,h5,mp,share}.log`。

PASS：Chrome实际H5构建在只读示例、CSP同源限制下检查DLT/SSQ各320×740、375×812、414×896、812×375，共8组尺寸、40张状态卡。成本/奖金标签Y坐标差均小于1px，右侧与卡片边缘间隔为现有内边距13/17px，页面及卡片无横向溢出；金额/0/待确认/长金额/旧接口兼容均保留。[大乐透](evidence/h5-dlt-375.png)、[双色球](evidence/h5-ssq-375.png)。临时页面与视口已清理。只读示例不作为真实业务结算验收。

后端测试/DEV数据库写入不适用：仅CSS变化，业务及接口未改；微信工具/真机及发布NOT_RUN（按用户阶段规则排除，非必需）。无未解决必需FAIL/BLOCKED。内容、相对链接及git diff --check通过后进行远端同步。

同步后Review PASS：任务文件stash→git pull --ff-only（Already up to date）→apply；逐文件SHA-256相同，恢复确认后删除备份。无冲突/远端上下文变化；复审本次3条样式和文档无问题，再执行回归/类型/产物检查。构建输入未变，既有双端构建仍对应当前源码。

同步后复验PASS：彩票相关7/7、TypeScript及25页微信分享产物通过；文档链接及git diff --check通过，暂无必需FAIL/BLOCKED。H5待提交推送后执行release.py h5，后端及数据库无需更新。


发布闭环PASS：`314a217 fix(lottery): 将开奖成本和奖金同行靠右显示`已推送GitHub origin/main并核对一致；`python3 deploy/local/release.py h5`退出0，2026-10-30 23:37:02+08:00发布。公开HTML、7个静态资源（含开奖页JS及全局CSS）和版本JSON逐字节匹配本地产物，静态版本5ed2d703-1045-4c59-b101-6ed1bc22b6a0；公开未登录开奖API401。Chrome线上375px全部20张卡片成本/奖金标签同行，首卡为“成本2奖金0”，无横向溢出；独立页面和视口覆写已清理。线上截图只保留/tmp，未提交实际用户金额画面。

后端无需更新，仍为b6106ed、running/0重启；数据库无SQL/Migration/配置操作。H5已实际更新成功，服务器h5-release匹配314a217，无上传/站点暂存目录。微信共享样式/类型/命令行构建已同步，需用户自行发布新版；工具/真机及发布NOT_RUN（按用户阶段规则排除，非必需）。本轮无未解决必需FAIL/BLOCKED；后续纯文档归档无需重复更新服务或客户端。
