# 验证

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 最早有效账单统计查询 | PASS（静态+单测） | Mapper 查询限定 `user_id`、`deleted=0`；`ProfileServiceTest` 覆盖服务调用。 |
| 自然日累计与无账单边界 | PASS | `ProfileServiceTest` 3 项测试通过。 |
| 数据库结构与 Flyway | PASS（范围核对） | 本次未变更 Schema 或 Migration。 |
| 全量后端回归 | PASS | `server: mvn test`，29 tests，0 failures/errors/skipped，BUILD SUCCESS。 |
| 前端类型检查与双端构建 | NOT_RUN（范围外） | 本次未改动前端源码或构建配置，未重复执行前端构建。 |
| 真实登录态个人资料接口 | NOT_RUN | 当前未取得有效会话。 |
| MySQL 执行计划/真实历史数据复核 | NOT_RUN | 未在本次运行连接 MySQL。 |
