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

## 独立物品验收（2026-09-28）

ItemServiceTest覆盖11项核心金额/日期/冻结/用户隔离/幂等/删除审计测试。ItemDevIntegrationTest默认跳过，显式-Ditem.dev.verify=true连接DEV依赖，通过真实HTTP RSA登录与6路并发创建，核对Flyway、information_schema与Entity。合成用户/物品在finally按精确ID清理，会话logout；不读取真实用户密码、不记录Token或验证码。H5只读夹具与真实API证据分开，微信构建不替代工具/真机。完整记录见 [物品验收](../10-iterations/2026/09/item-lifecycle/09-verification.md)。

真实H5验收可显式追加-Ditem.ui.verify=true：ItemUiVerifier只在测试代码中启用localhost短期桥接，真实会话只驻留内存；手工从浏览器新增/退役后通过target/item-ui-complete结束，测试继续验证物品已退役和记账零影响并清理。默认测试不启动此服务，不输出/保存凭证。已执行证据见dev-ui.json；真实页面删除按钮提交和微信工具/真机仍NOT_RUN。

## 验证与自动提交门槛

按 [长期工程规则](../../AGENTS.md) 的“验证、同步与自动提交”执行：本次改动的必需测试和构建通过后，拉取当前分支 upstream，解决冲突并复验最终代码，再自动提交并推送到当前分支 upstream。必需验证 FAIL、BLOCKED 或 NOT_RUN 时不得自动提交；纯文档变更只做内容、链接及 Git 空白检查，不要求无关业务构建。远端更新影响代码或依赖时，即使无冲突也需复验。提交前审查暂存差异，只包含本次任务，并使用 `<type>(<scope>): <中文摘要>`。
