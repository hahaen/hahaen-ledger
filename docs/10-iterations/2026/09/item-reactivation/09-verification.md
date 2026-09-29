# 验证状态

| 范围 | 状态 | 证据或限制 |
| --- | --- | --- |
| 需求、状态与成本逻辑 | PASS | 定向 Service 测试 14 项，覆盖字段清空与重算。 |
| DEV Migration、API、权限、物品数据 | PASS | Flyway V8 与隔离合成用户集成测试成功；数据库读回 `retired_on` 和 `resale_cent` 为 NULL。 |
| 重复提交与旧请求 | PASS | 同键重试仅一次写入，旧退役/重新服役键的状态变化冲突经单测与集成测试检查。 |
| 前端类型与双端构建 | PASS | TypeScript、H5 和微信小程序构建均完成。 |
| 真实 H5 详情交互 | NOT_RUN | 尚未在浏览器点击重新服役确认层。 |
| 微信开发者工具与真机 | NOT_RUN | 仅执行小程序构建。 |
| V8 关系表结构 | PASS | `information_schema` 核对 5 列、非空中文注释、`created_at` 与 `deleted` 的可空/默认值。 |
| 前端全量测试 | PASS | 87 项通过。 |
| 后端全量测试 | FAIL | 68 项中 `LoggingProfileConfigTest` 在 macOS 下断言 Windows 固定日志路径失败；物品定向与集成测试通过。 |
