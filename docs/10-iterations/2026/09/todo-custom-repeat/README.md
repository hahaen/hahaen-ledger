# 待办自定义重复

- requirement：新增/编辑参考用户五张截图的重复逻辑，不模仿样式。三模式：按时间、完成后、固定日；1–365天/周/月/年；星期/月日多选、月末、年度月日、固定公历日期。
- design：沿用薄荷卡片与按钮。按时间保留现有滚动生成；完成后只保留一个未完成项，实际完成时间起算；固定日有限序列。月内不存在的日期落月末，同月同日去重。周以周一为基准，年度日期为公历。截图农历仅辅助展示，不实现农历规则。
- database：新增V11，两张业务表保存相同规则字段与历史快照；V10不修改，已有规则兼容。
- api：新增CUSTOM及模式/单位/间隔/星期/月日/月末/年度月日/固定日期字段，旧请求兼容。
- backend：权限、事务、幂等和历史保留沿用现有契约；完成后规则禁止定时提前生成。
- frontend：新增与编辑共用规则表单，回填/校验/保存重试/列表摘要一致；uni-app基础组件适配H5与微信。
- testing：计划执行规则边界、真实DEV数据库/API验证、前端交互回归、类型与双端构建。
- commands：待记录实际执行结果。
- verification：NOT_RUN（实施前）。
- rollback：回退应用代码前先将CUSTOM规则停用；V11新增字段保留，不删除已执行Migration。

## 最终实现与验证

用户追加要求已落实：删除重复预设下拉，三模式和全部规则内容直接展示；重复开关关闭时表单禁用仍可见。单位改为页内四按钮，年度月份用左右按钮，避免双端原生滚轮交互差异。

| 验证 | 状态 | 证据 |
| --- | --- | --- |
| 后端边界、兼容、提醒定向回归 | PASS | TodoScheduleTest 3、TodoRepeatTest 7、TodoReminderWorkerTest 1，无失败 |
| DEV数据库与真实HTTP | PASS | TodoDevIntegrationTest与TodoApiDevIntegrationTest各1；V11、information_schema/Entity列一致、注释/字符序、实际完成时间起算、幂等、历史、周多选、固定日期耗尽和归属隔离 |
| 前端规则和页面流回归 | PASS | node --test共39项，包含新增校验、失败键重试、编辑回填、月末保存、隐藏字段清理与摘要 |
| 类型检查、H5与微信生产构建 | PASS | 三命令均退出0；Java 25后端打包退出0 |
| H5交互 | PASS（已测范围） | 三模式切换、15/31日多选、月末开关、固定日翻月与10-01/10-15多选；DOM无横向溢出；截图下方 |
| 微信模拟器交互 | PASS（已测范围） | 重复开关、单位月按钮、15/31日与月末选中、完成后隐藏日期控件、固定日翻月与10-01/10-15选中；Stable 2.02.2608070，iPhone15ProMax模拟器 |
| 本地9898后端 | PASS | 重启打包后的新代码，Started LedgerApplication、端口9898启动、公开password-key HTTP200，Flyway11已执行 |
| 两端客户端完整新增→编辑→完成持久化链路 | PARTIAL | 实际表单交互+前端保存回归+真实DEV HTTP已测；未在真实用户会话写入临时待办，不将分层证据表述成完整点击端到端 |
| 微信真机、所有尺寸 | NOT_RUN（补充验收） | 本轮模拟器及单次窄视口验证，不宣称真机或全尺寸通过 |
| Bark/pushplus实际送达 | BLOCKED（既有外部能力） | 未使用真实通知密钥与接收设备；本次重复规则验证不依赖外部发送 |

必需门禁为规则/权限/事务/数据库真实验证、受影响前端回归、类型、双端构建和双端规则交互，均PASS。真实终端送达、真机与完整真实用户点击链路为补充验收，保留边界；项目既有全量回归状态不据本次定向测试改写。

### 实际命令

- Java25：`mvn -q -Dtest='TodoScheduleTest,TodoRepeatTest,TodoReminderWorkerTest,TodoDevIntegrationTest,TodoApiDevIntegrationTest' -Dtodo.dev.verify=true test`，13项无失败/跳过，退出0。
- Java25：`mvn -q -DskipTests package`，退出0。
- app：`node --test tests/todo-repeat.test.mjs tests/page-flows.test.mjs`，39项PASS。
- app：`pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin`，全部退出0。
- app：`pnpm run dev:mp-weixin`，实际编译完成并在现有开发者工具加载；不计作生产构建替代。
- 最初typecheck发现input/switch事件类型错误，修复后复验PASS；最终生产双端构建与回归基于页内选择版本。
- 本地后端使用Java25打包JAR启动（9898），公开认证接口HTTP200。
- Git远端同步/暂存/提交/推送以本次Git历史与交付报告为准。

### 截图

- [H5月重复](evidence/h5-monthly.png)
- [H5固定日期](evidence/h5-fixed-dates.png)
- [微信月重复](evidence/wechat-monthly.png)
- [微信固定日期](evidence/wechat-fixed-dates.png)

全页H5截图的固定导航随采集时滚动位置显示，属于截图捕获行为；交互验证以DOM选中状态与实际操作为证据。所有截图只证明其实际可见区域，不推断画面外持久化。

### 远端同步

`git stash push --include-untracked`安全保存33个本任务文件，`git pull --ff-only`返回Already up to date，无冲突；恢复后逐文件SHA256与备份一致、diff --check通过，再删除stash备份。同步后复跑受影响的前端类型/39项回归及13项后端定向验证；构建代码与同步前逐字节一致。
