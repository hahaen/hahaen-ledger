# 07｜测试规范与当前证据

测试需要覆盖业务正确性、权限隔离、数据库约束、金额事务、幂等、平台差异和运行依赖，不能只列出构建成功。

## 已执行的当前工作区检查

- `server/mvn test`：18 个测试，0 failures/errors/skipped，覆盖账户、资产、认证密码、日历、Redis DAO、异常、金额、首页、账单和个人资料单测。
- `app/pnpm run typecheck`：通过。
- `app/pnpm run build:h5`、`app/pnpm run build:mp-weixin`：在注入本地 API 地址后通过。
- 当前运行探针可访问 8080 的 `/api-docs` 和验证码接口，未登录账户接口返回 401；这不等于真实数据库账务联调通过。

## 尚未替代的验证

Mockito/纯逻辑 Service 测试不能替代真实 MySQL Mapper、Flyway、事务隔离、并发退款和跨用户数据库测试。H5 有效会话刷新、真实头像上传、微信开发者工具、微信登录和 320/375/414 逐区域视觉验收必须分别记录证据。没有执行的项目写 `NOT_RUN`，外部依赖不可得写 `BLOCKED`。

## 结果记录

每条结果至少写命令、前提、实际输出、代码/文件定位和未覆盖范围；只有已执行且符合预期才可写 `PASS`，构建不能替代运行或业务验收。

## 核心页面回归（2026-09-08）

node --test tests/entry.test.mjs tests/page-flows.test.mjs 通过 9 项前端回归；mvn test 通过 24 项后端单测，交易测试 9 项。类型检查和双端构建通过。详见 [核心页面迭代](../10-iterations/2026/09/core-pages-prototype/README.md)，其中的只读视觉证据不能替代数据库事务和真实账务写入。

## 个人中心（2026-09-11）

`ProfileServiceTest` 覆盖当前用户昵称更新、已设账号不可改、首次资料保存时账号与密码同写及重复账号拒绝；前端 `page-flows.test.mjs` 覆盖首次密码随资料保存请求提交和成功返回弹层。实际命令及限制见 [profile-center-management](../10-iterations/2026/09/profile-center-management/README.md)。

## 头像对象 Key 与去重（2026-09-11）

`AppFileServiceTest` 覆盖同一用户、同一 SHA-256 的已就绪头像直接复用既有对象 Key 且不插入新文件元数据。该单测不替代真实 MySQL/Flyway、MinIO PUT、服务端回读和 H5 已登录会话验证；这些项目必须保持独立证据。
