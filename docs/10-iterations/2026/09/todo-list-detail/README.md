# 待办列表与详情交互整改（2026-09-30）

- requirement：我的入口勾形居中；新增去掉加号；列表圆圈垂直居中并点击完成，其余区域进入详情；列表不放完成/修改/删除按钮；待完成右侧黄色距离/过期天数，已完成右侧已完成；详情中待完成可修改规则与删除，已完成仅底部删除；追加反馈：清单元信息移除到期提醒，开启提醒仅在卡片右上角显示提醒角标。
- design：沿用薄荷绿与项目暖黄，独立详情复用公共导航及白色卡片；圆圈采用48px点击区域与中心点；完成成功才显示中心点，失败保留重试。
- database：无变更，保留发生项/规则/历史快照隔离。
- api：复用todoApi详情、完成、编辑、删除及既有幂等协议。
- backend：实际模拟器发现详情接口仅支持PENDING，改为查询当前用户未删除发生项，支持COMPLETED快照；编辑仍仅PENDING，数据库无迁移。
- frontend：新增详情路由；列表圆圈阻止点击冒泡，详情返回列表与编辑返回详情刷新。
- testing：完成防重/失败重试、待完成与历史详情、删除确认与重试、天数边界、前端回归、类型与双端构建、界面核验。
- commands：见下方真实执行记录。
- verification：必需回归/类型/双端构建/DEV接口与权限/Java打包PASS；实际客户端全链路PARTIAL，见下方边界。
- rollback：revert本轮前后端与文档提交，无数据库回退。


## 真实验证记录

- app：`node --test tests/todo-list-detail.test.mjs tests/todo-repeat.test.mjs tests/page-flows.test.mjs`，47/47 PASS；包含完成防重、同键重试、详情失效链接/重试、历史只读、删除确认/重试、直接打开详情返回、编辑保存返回详情、北京时间期限边界。
- app：`pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin`，全部退出0；Sass既有弃用提示保留。
- server（Java25）：`mvn -Dtest=TodoApiDevIntegrationTest,TodoDevIntegrationTest,TodoScheduleTest,TodoRepeatTest,TodoReminderWorkerTest -Dtodo.dev.verify=true test`，13项无失败/错误/跳过。真实MySQL/Redis与随机HTTP端口的合成账号覆盖已完成快照、completedAt、另一用户详情被拒、完成记录编辑被拒、规则编辑不改变历史标题、删除完成记录后详情不可见。合成数据由原有测试finally清理；不调用外部通知。
- server（Java25）：`mvn -DskipTests package`，退出0；测试由前一条单独执行。
- 实际微信Stable 2.02.2608070/iPhone15ProMax：入口图标居中；待完成3/已完成2读取成功；列表无三个文字操作按钮，提醒角标、右侧状态显示；内容点击待完成详情/返回成功。已完成详情初测发现旧后端pending限定错误，随后修复并经真实HTTP验证。现有9898仍为旧Java进程，本轮未重启，微信新服务历史详情复测PARTIAL。
- 微信最初因路由先于新页面落盘出现缺失wxml；本轮启动development watcher完整重编译后模拟器恢复，无缺失文件错误。生产构建也通过。
- H5隔离夹具（独立本地18762，不调用真实用户API）：375×812已测；上下留白初测3px偏差修正为对称20px，最终两个圆圈中心偏差均0px；卡片右上提醒、黄色期限、点击内容详情/修改规则/取消返回、完成后数量2/1变为1/2、已完成详情只删除、确认删除返回数量1/1均PASS。320×568、414×896、812×375读取innerWidth与documentElement.scrollWidth分别相等，无横向溢出。合成夹具写入不代表真实用户客户端写入。
- 初次命令有工作目录/相对文件路径错误，已在正确app/server目录重跑；上述统计只使用最终成功记录。H5在构建更换资源期间曾有旧chunk加载超时，最终构建刷新后页面与交互通过。
- 数据库：无Schema/迁移变更；已有Flyway V11、information_schema/Entity校验由TodoDevIntegrationTest实际PASS。
- 补充验收：微信真机、真实用户完成/删除操作NOT_RUN；外部通知送达BLOCKED沿用既有边界。本轮权限/服务运行由真实DEV隔离测试提供证据，未部署或发布。

[H5待完成](evidence/h5-pending.png) · [H5已完成](evidence/h5-completed.png) · [H5待完成详情](evidence/h5-detail.png)

## 同步与交付

同步、提交与推送结果以本轮Git命令与最终报告为证据；只提交本次文件，逐文件暂存，禁用force push。

实际同步：stash备份22个本轮文件（含未跟踪文件），git pull --ff-only返回Already up to date；恢复后22个文件SHA256全部一致，确认后删除stash。远端没有相关代码/依赖更新，已验证构建与同步后源码一致；同步后定向前端回归和类型检查再次执行。
