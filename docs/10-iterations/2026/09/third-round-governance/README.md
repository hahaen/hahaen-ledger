# 第三轮工程治理（含历史数据库过程）

> 数据库当前事实以 `flyway-v1-baseline` 为准：唯一初始基线是 V1；本目录中的 V3/V4 描述是历史执行证据，不是当前迁移顺序。

- feature-key：`third-round-governance`
- 类型：工程治理 / 数据库治理 / 文档重构 / 设计与回归验收
- 日期：2026-09
- 状态：PARTIAL；本轮 Flyway V3 已在真实 DEV 数据库复验通过，真实微信平台和完整业务回归仍保持 BLOCKED/NOT_RUN。
- 变更范围：V3 审计字段与中文注释、Java 公共实体与自动填充、软删除审计、前端交易状态刷新、AGENTS 和 docs 分层。

本目录各文件分别记录需求、设计、数据库、API、后端、前端、测试、命令、验证和回滚；不得把未执行结果写成 PASS。

后续 `audit-field-defaults` 已将公共字段规则更新为仅 created_at 必填且默认当前时间，deleted 默认 0；V4 与真实 DEV 验证 PASS，详见对应迭代，本目录保留 V3 历史过程。
