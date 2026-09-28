# 分层验收（2026-09-28）

| 范围 | 状态 | 证据和限制 |
| --- | --- | --- |
| 独立域/权限/幂等/金额/日期/退役冻结/删除审计单测 | PASS | ItemServiceTest 11/11；按购买当天第1天，跨闰日、零价、负净成本、原创建摘要重试、退役冲突及归属拒绝。 |
| 真实DEV依赖与生命周期HTTP | PASS | ItemDevIntegrationTest显式执行1/1：真实RSA登录、6并发创建只生成1条、跨用户详情/退役/删除拒绝、未来日期拒绝、退役与重复/冲突、删除审计、删除后不可查询、账户汇总一致且无新增账单。 |
| Flyway/information_schema/Entity | PASS | DEV haji_dev已执行V6；22列与Entity及其基类一致，中文COMMENT、10个公共字段可空/default及datetime(3)核对。详见evidence/dev-api.json。V1–V5未改，V6已执行后不得改写。 |
| 前端回归/类型/双端构建 | PASS | 82/82；vue-tsc通过；H5、mp-weixin生产构建完成。 |
| H5视觉及交互夹具 | PASS | 320/375/414无横向溢出、5导航；实际canvas图、添加必填错误/失败保留、退役/删除取消及负成本展示。截图与layout-checks.json。只读夹具不证明真实页面写入。 |
| Java打包 | PASS | Java25 -DskipTests package成功；不将跳过测试等同于完整回归通过。 |
| 完整后端回归 | FAIL | 65项：63通过、1旧日志目录断言失败、1集成测试默认跳过；该集成测试单独运行PASS。旧失败在开始时HEAD已存在，与物品改动无关。 |
| 真实H5隔离登录态添加/退役写入 | PASS | ItemUiVerifier临时内存会话桥接正式H5构建至真实DEV服务：页面添加399.99元、详情第1天、以100元售价退役、最终299.99元成本、返回汇总归零和退役条目均验证；dev-ui.json和4张dev-ui截图。真实页面删除按钮未提交，真实删除API和确认取消已独立验证。 |
| 微信开发者工具/真机交互 | NOT_RUN | 本机工具存在，但本轮未导入/运行或操作真实微信身份，仅构建通过。 |
| 正式部署 | NOT_RUN | 未发布或操作正式环境。 |
| 整体验收 | PARTIAL | 新功能代码、数据库、真实API、视觉夹具和构建已交付，剩余微信工具/真机及真实H5删除按钮提交为NOT_RUN，完整后端旧测试FAIL单列。 |

没有新发现的外部依赖BLOCKED。现有8080 Java服务为开始工作前进程；本次真实DEV测试另启随机端口且退出后关闭，不中断原进程。使用8080新API需重启现有后端。

## 变更交接

- 修改：AGENTS.md、README.md、当前00/01/02/03/04/07/08规范、审计矩阵/第三轮审计/第三轮迭代及索引；app两份pages.json、BottomNav、prototype.scss、api.ts、只读visual-server。
- 新增：server/item Controller/Service/Costs/Mapper/Entity/DTO/VO、V6、11项单测及DEV集成测试；items/item-detail页面、ItemEditor/ItemCostChart、items.ts、items.test.mjs；物品API契约、本迭代01–10与截图/JSON证据。
- 忽略本地：本轮未修改本地配置、凭证、前端env；正常重建target/dist产物。
- 预先存在：工作区起始干净，main已有两个未推送提交；未提交/推送本轮修改。既有日志配置与测试不一致保持单独FAIL。
