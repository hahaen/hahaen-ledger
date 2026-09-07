# 09｜验证

当前结论：`PARTIAL`。

## PASS

- V3/V4 Entity、Mapper、Service、Controller 和前端关联页已实现；代码编译通过。
- `mvn test`：18 tests，0 failures/errors/skipped；覆盖账户边界、净资产口径、跨用户资源不存在、账单余额影响、退款有效金额、首页统计和闰年月历。
- `pnpm run typecheck`、H5 生产构建（临时本地 API 地址）、微信小程序生产构建通过。
- 独立端口 18080 启动成功，未登录业务接口返回 401，OpenAPI 暴露本轮 12 个路径；Redis PING 和 MinIO bucket 探针成功。
- `git diff --check` 通过；Flyway Migration 目录无差异，未执行 DDL。

## PARTIAL / BLOCKED / NOT_RUN

- PARTIAL：真实登录用户下的首页、日历、资产读写联调未完成；单元测试不能替代真实数据库事务。
- PARTIAL：设计稿逐区域截图验收未在微信开发者工具和多尺寸设备执行。
- BLOCKED：没有隔离测试用户/真实微信会话，未执行真实账单写入、退款并发、跨用户数据库查询和回滚证据，避免污染开发库。
- NOT_RUN：微信开发者工具实际交互、MySQL `information_schema` 对照、真实 H5 会话刷新和前后端完整联调。

最终状态按功能、测试、构建、运行、设计截图和禁止修改范围分别记录 PASS/PARTIAL/FAIL/BLOCKED/NOT_RUN，并链接到具体文件和命令证据。
