- [2026-09 本地 dev 连接凭证隔离](2026/09/local-dev-credentials/README.md)：将个人连接与账号密码转入 Git 忽略的本机配置文件。
# 10｜迭代档案

- [2026-09 Jenkins 与 Docker Compose 部署脚本](2026/09/jenkins-compose-deployment/README.md)：为前后端任务提供 SSH/Compose 部署基础。
- [2026-09 正式 MinIO 预签名 PUT 代理修复](2026/09/minio-presigned-put-proxy/README.md)：补齐 Nginx 到 MinIO 的签名 PUT 反向代理配置，线上探针已通过，真实页面回归待用户重试确认。
- [2026-09 物品清单排序](2026/09/item-list-order/README.md)：物品清单在役优先，同状态按购买日期由新到旧排列。
- [2026-09 物品资料编辑](2026/09/item-profile-edit/README.md)：物品详情编辑名称、购买价格和购买日期。
- [2026-09 已退役物品重新服役](2026/09/item-reactivation/README.md)：清除退役日期和二手价格，恢复在役成本。
- [2026-09 物品价格输入格式](2026/09/item-price-input-format/README.md)：新增占位显示 0，编辑整元回显不显示 `.00`。
- [2026-09 物品清单净成本](2026/09/item-list-net-cost/README.md)：清单行价格读取实际净成本，退役时扣除二手售价。
- [2026-09 物品日期弹窗年份选择](2026/09/item-date-picker-year/README.md)：物品日期统一从 2000 年开始，年份栏保持普通列表样式。
- [2026-09 日期弹窗滚动修复](2026/09/date-picker-scroll-stability/README.md)：年份完整显示 2000–2099，修复月/日跳回，物品仍不能保存未来日期。

本目录按 `YYYY/MM/feature-key/` 保存历史变更。每个重大功能、Bug、数据库、权限、基础设施或设计整改，都应有一个独立 feature-key，避免把多次变更混成无法审计的长文档。

## 固定文件

| 文件 | 说明 |
| --- | --- |
| `README.md` | 本次迭代摘要、边界、状态和文件导航。 |
| `01-requirement.md` | 需求来源、目标、范围、非目标和验收条件。 |
| `02-design.md` | 方案、关键决策、替代方案和影响面。 |
| `03-database.md` | Migration、字段、索引、约束和数据风险。 |
| `04-api.md` | 接口、DTO/VO、错误、权限和幂等变化。 |
| `05-backend.md` | Entity、Mapper、Service、Controller、事务和日志实现。 |
| `06-frontend.md` | 页面、状态、API 调用、平台差异和交互变化。 |
| `07-testing.md` | 测试范围、用例、依赖、预期和实际结果。 |
| `08-commands.md` | 真实执行的命令及结果，不放猜测命令。 |
| `09-verification.md` | 分项状态和证据，区分 PASS/PARTIAL/FAIL/BLOCKED/NOT_RUN。 |
| `10-rollback.md` | 未执行和已执行两种情况下的安全回滚策略。 |

## 编写边界

历史档案可以记录当时的缺口，但不能将后续实现倒灌成当时已完成；共享环境执行过的 Migration 不修改、不删除，结构变化通过新的 Migration 记录。

- [2026-09-12 后端日志目录](2026/09/backend-log-directories/README.md)：为 dev 与 prod Profile 配置独立的文件日志目录。

- [2026-09-13 H5 静态目录部署权限](2026/09/h5-static-deploy-permission/README.md)：避免 Jenkins 部署用户对既有 Nginx 静态目录执行无权限 chmod。

- [2026-09-12 登录注册协议勾选与协议页面](2026/09/auth-legal-agreements/README.md)：登录与注册提交前同意协议，新增未登录可读的用户协议和隐私协议页面。
- [2026-09-12 账号字符限制与注册昵称默认值](2026/09/account-alphanumeric-nickname-default/README.md)：账号仅限英文字母和数字，首次注册昵称默认使用账号。

- [2026-09-08 核心页面原型与流程修复](2026/09/core-pages-prototype/README.md)：保留资产/我的页，补齐记账、还款编辑、退款及状态联动。
- [2026-09 账单详情退款弹窗设计对齐](2026/09/transaction-refund-modal-design/README.md)：按参考稿统一退款弹层布局，保持既有退款业务链路。
- 该迭代后续补充修复退款记录删除成功后的详情刷新和页面状态问题，详见同一档案的追加需求与验证记录。

- [微信小程序全页面自定义导航对齐](2026/09/wechat-custom-navigation-alignment/README.md)：修复小程序页头与微信胶囊按钮的位置及行高。
- [微信小程序转发](2026/09/wechat-mini-share/README.md)：为全部小程序页面接入微信右上角转发，统一安全地回到首页。

- [2026-09-28 独立物品管理](2026/09/item-lifecycle/README.md)：五导航、成本口径、V6迁移、真实DEV接口和视觉证据。
- [2026-09-29 物品清单退役行灰底](2026/09/item-retired-row-background/README.md)：按退役状态区分列表行底色。
