# 验证

| 项目 | 状态 | 证据/限制 |
| --- | --- | --- |
| Mapper 排序静态核对 | PASS | SQL 排序键为 ACTIVE 优先、购买日期倒序、ID 倒序；Service 后续筛选和分页复用该顺序。 |
| API 契约文档同步 | PASS | `docs/03-api/items.md` 已记录规则。 |
| 自动化测试、类型检查和构建 | NOT_RUN | 未执行。 |
| DEV API、页面与跨端验收 | NOT_RUN | 未执行。 |
