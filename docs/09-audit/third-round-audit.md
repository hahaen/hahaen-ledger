# 第三轮工程审计报告

审计日期：2026-09-06。本次补充审计聚焦 H5 我的页、关于与帮助、用户累计天数、头像文件和退出登录，不把未执行的运行联调写成 PASS。

## 本轮结论

- 我的页与原型的个人卡片、累计天数、更多分组、关于与帮助、退出登录已落到 H5 页面。
- 累计记账天数由服务端 `ProfileService` 依据当前用户 `app_user.created_at` 计算，创建日计为第 1 天，并有单元测试证据。
- 头像上传复用现有 MinIO 预签名 URL 链路，文件查询补充当前用户和逻辑删除过滤；前端仅使用短时效预览 URL。
- H5 退出登录在 store 中清理服务端会话对应的本地令牌和业务状态，mine 页保持当前页面并显示“登录”。
- H5 刷新有效会话时不再无条件跳首页；Sa-Token 会话改用 Redis DAO，并对 Redis Key 增加 `haji:` 前缀。
- 用户启动日志发现 Redis DAO 自动配置与自定义 DAO 重复注册；已通过排除自动配置保留单一自定义 DAO，避免 Spring 上下文启动失败。
- 前端 typecheck、H5 构建、后端编译和测试 PASS；真实 H5 登录态刷新为 PARTIAL，后端重启续会话和 MySQL/Redis/MinIO 联调为 BLOCKED，微信开发者工具验收仍为 NOT_RUN。

## 仍需人工验收

1. 启动 MySQL、Redis、MinIO，使用真实 H5 会话验证刷新保留当前路由、后端重启续会话、个人资料、头像上传/刷新、旧预览 URL 过期重取和退出后的“登录”状态。
2. 在 320/375/414px 下截图对照原型，检查滚动、安全区和帮助页长内容。
3. 后续补齐微信小程序头像选择和平台差异，不把 H5 文件选择器直接移植到微信端。

## 本轮追加：资产账户数据库设计

- 已新增 V3 `asset_account` 统一账户表，使用 `FUND`/`CREDIT` 区分资金账户和信贷账户，并用数据库约束维护金额字段的类型对应关系。
- 账户表包含用户归属、净资产标识、完整公共审计字段、逻辑删除字段和资产页查询索引；账户名称允许重复，当前单账本模型不重复保存账本字段。
- 本轮仅完成 SQL 与文档；未修改 Java、TypeScript 或业务接口，也未实际执行 Flyway/MySQL。数据库执行和 `information_schema` 对照保持 `NOT_RUN`，不能据此宣称运行通过。

## 本轮追加：账单明细与退款数据库设计

- 已新增 V4 `transaction_detail` 账单明细主表和 `transaction_refund` 独立退款关联表。
- 主表使用 `EXPENSE`、`INCOME`、`TRANSFER`、`REPAYMENT` 四类账单；退款不占用账单类型，并以 `transaction_id` 关联原始账单。
- 金额使用整数分，保留 `original_amount`，以 `amount` 表示有效金额，并使用 `has_refund` 记录是否曾发生退款；V4 静态约束覆盖金额范围、字段适用形状、编号唯一性和查询索引。
- 当前为单账本模型，V4 不在 `transaction_detail` 重复保存 `book_id`；用户/账户归属和退款累计事务由后续 Service 完成。
- V4 尚未在 MySQL/Flyway 中执行，`information_schema` 对照和退款运行时事务用例为 `BLOCKED`/`NOT_RUN`，详见 `docs/10-iterations/2026/09/transaction-detail-refund-schema/09-verification.md`。
