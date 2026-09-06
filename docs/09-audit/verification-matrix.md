# 当前验证矩阵

更新日期：2026-09-06。状态只表示本轮实际证据：PASS=已执行且符合预期；PARTIAL=部分完成；BLOCKED=外部条件不可得；NOT_RUN=尚未执行。

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 我的页原型内容 | `app/src/pages/mine/mine.vue`、原型 `app.js` 的 `mineView` | PASS |
| 关于与帮助原型内容 | `app/src/pages/help/help.vue`、原型 `app.js` 的 `aboutView` | PASS |
| 累计记账天数 | `ProfileService` 按 `app_user.created_at` 计算，`ProfileServiceTest` 覆盖创建日/前一日/未来日期 | PASS |
| H5 头像上传 | `AppFileService` + MinIO 短时效上传/确认/预览接口，前端不持有密钥；真实对象上传未联调 | PARTIAL |
| H5 退出后停留当前页 | `ledger.logout` 清理本地会话，mine 页切换为“登录”状态 | PASS（代码） |
| H5 刷新保留当前路由 | `App.vue` 有效会话不再无条件跳首页，仅根路径补到首页 | PASS（代码）；真实登录态刷新为 PARTIAL |
| Sa-Token 会话持久化 | `sa-token-redis-jackson` + `SaTokenRedisConfig`，Key 统一加 `haji:` | PASS（代码/依赖）；跨重启联调 BLOCKED |
| 当前用户与逻辑删除过滤 | `ProfileService`、`AppFileService` 按当前用户并过滤 `deleted=0` | PASS（代码） |
| 前端类型检查 | `pnpm run typecheck` exit 0 | PASS |
| H5 生产构建 | 注入本地 API 地址后 `pnpm run build:h5` exit 0；未配置地址时按规则拒绝 | PASS |
| 后端测试 | `mvn test`：4 tests，0 failures/errors/skipped | PASS |
| H5 真实登录态运行 | H5 登录页可打开，但本机后端不可用，验证码请求失败 | PARTIAL |
| MySQL/Redis/MinIO 联调 | 本机当前未监听 MySQL/Redis；MinIO 也未形成本轮有效会话证据 | BLOCKED |
| 微信小程序运行 | 用户明确安排后续补齐 | NOT_RUN |
