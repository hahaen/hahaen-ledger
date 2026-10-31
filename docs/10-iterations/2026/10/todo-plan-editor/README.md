# 待办计划表单与逾期编辑修复

2026-10-31。实现、首轮Review、前后端验证及真实DEV/H5验收PASS；upstream同步复审复验PASS；待提交推送和自动发布后端/H5。

新增/编辑统一不重复、按时间、完成后、固定日期；新增默认不重复。开始日期和执行时刻分开，预览由正式后端计算。逾期原时间、旧单次与旧周期、秒/毫秒精度保留；仅改内容/提醒原地更新所有未完成项，真正重排才替换，完成历史不变。

本轮无Schema/Migration或配置操作。微信代码兼容及命令行构建PASS，工具/真机/上传发布NOT_RUN（按用户阶段规则排除，非必需），新版需用户独立发布。

文件：[需求](01-requirement.md)、[设计](02-design.md)、[数据库](03-database.md)、[API](04-api.md)、[后端](05-backend.md)、[前端](06-frontend.md)、[测试](07-testing.md)、[实际命令与Review](08-commands.md)、[验收](09-verification.md)、[失败处理](10-rollback.md)。
