# 验证

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 前后端账号规则与注册昵称 | PASS | 前端流程回归 29/29；定向后端测试 12/12 验证注册昵称与规范化账号一致，且中文、点号、下划线、连字符均被拒绝。 |
| TypeScript 类型检查 | PASS | `pnpm run typecheck` 退出码 0。 |
| 后端全量测试 | PASS | `mvn test`：40 tests，0 failures/errors/skipped，BUILD SUCCESS。 |
| H5/微信小程序生产构建 | PASS | 设定本地 API 基址后两项生产构建均完成。 |
| 真实登录、注册和个人中心交互 | NOT_RUN | 本次未执行。 |
