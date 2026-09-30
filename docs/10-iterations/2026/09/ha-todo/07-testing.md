# 测试

- `TodoScheduleTest`：31 日跨短月并返回 31 日、闰年 2 月 29 日、隔三个月和一次规则，PASS。
- `TodoReminderWorkerTest`：模拟平台受理，核对渠道、实际消息标题/正文、发送尝试记录与投递状态，PASS；不等于真实送达。
- `TodoDevIntegrationTest`：DEV Flyway V10、五表字符序/注释、规则/发生项 Entity 列集合及生成，PASS；合成数据清理。
- `TodoApiDevIntegrationTest`：双合成用户真实 H5 登录后的 HTTP CRUD、重复项下一次生成、幂等重试、跨用户拒绝、历史快照与提醒记录读取，PASS；不发送外部通知，合成数据清理。
- `pnpm run typecheck`、`build:h5`、`build:mp-weixin`：PASS。
- 全后端 `mvn test`：FAIL，78 项中 1 个既有 `LoggingProfileConfigTest` 断言期望旧 Windows DEV 日志目录，实际配置为本机 macOS 路径；与待办清单代码无关。其余 77 项无失败，2 项显式 DEV 测试默认跳过，专项另跑通过。
- Chrome H5 实际新增每隔两月、完成后下一次、完成后编辑下次计划时间与历史快照、删除未完成和完成记录：PASS。微信开发者工具模拟器入口、新增一次、完成、数量切换、删除：PASS；原生重复选择器完整持久化 PARTIAL。逐项结果见 [客户端测试报告](11-client-test-report.md)。
- 微信真机和 Bark/pushplus 实际投递：前者 NOT_RUN，后者 BLOCKED（未使用真实通知凭证和接收设备）。

2026-09-30 独立表单页补测：`TodoApiDevIntegrationTest` 增加本人详情可读、他人及已完成详情拒绝断言，定向 Java 测试 PASS。H5 实际点击新增、编辑、取消保护、编辑页刷新后回填、保存后回清单和 QA 删除 PASS；微信模拟器实际点击相同流程并清理 QA 数据 PASS。H5 卡片操作和编辑页按钮文字中心偏差实测 0–1 px；微信模拟器目视复查新增、完成、修改规则、删除和底部按钮，页头避开安全区；统计卡维持原布局，均为本次视口 PASS。完整逐项证据见[客户端测试报告](11-client-test-report.md)。
