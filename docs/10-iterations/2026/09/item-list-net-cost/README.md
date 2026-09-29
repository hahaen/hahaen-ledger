# 物品清单显示实际净成本

## Requirement

物品清单行中的价格显示实际净成本。在役物品尚无二手售价，净成本等于购入价；已退役物品按购入价减二手售价显示，允许为负数。日均成本和顶部汇总保持既有口径。

## Design

列表继续使用 `ItemVO.netCostCents`，金额经共享 `MoneyDisplay` 显示。与详情页“实际净成本”使用同一接口字段，避免前端重复计算。

## Database

无结构、数据或 Migration 变更。

## API

不改响应结构；`GET /api/app/items` 的每项已包含 `netCostCents`。

## Backend

不改服务端计算；`ItemCosts.view` 已按退役状态扣除二手售价。

## Frontend

物品清单价格位改读 `netCostCents`。详情购买价格、顶部在役购入总价、日均成本仍按现有字段显示。

## Testing

现有 `ItemServiceTest` 覆盖在役净成本与售价高于购入价的负净成本；物品前端回归覆盖输入、日期和导航。H5 只读示例页含两件在役物品、一件售价高于购入价的退役物品；截图可核对退役行 `-200 元`，其原购入价为 `3,000 元`。退役未出售场景按服务端公式与字段绑定静态核对，未单独运行页面用例。

## Commands

- `node --test tests/items.test.mjs`：退出码 0，5/5。
- `pnpm run typecheck`：退出码 0。
- `pnpm run build:h5`、`pnpm run build:mp-weixin`：均退出码 0。
- `mvn -q -f server/pom.xml -Dtest=ItemServiceTest test`：默认 Java 17 运行，class file 69 不兼容，退出码 1；随后用本机 Java 25 设置 `JAVA_HOME` 重跑，退出码 0。
- `git diff --check`：退出码 0。
- `node tests/visual-server.mjs`：启动只读示例服务；浏览器打开 `/#/pages/items/items`，观察 H5 清单与截图。

## Verification

PASS：清单绑定 `netCostCents`；物品回归 5/5、类型检查、双端构建和 Java 25 定向服务端测试通过。PASS（只读 H5 示例）：在役耳机为 `1,899 元`，退役相机原价 `3,000 元`、售价 `3,200 元`，清单显示 `-200 元`；[页面截图](../../../../../app/tests/evidence/item-list-net-cost-20260929.png)。PARTIAL：页面示例数据来自只读服务，未证明真实数据库与登录态接口显示。NOT_RUN：真实 H5 登录态、微信开发者工具及真机页面验收。

## Rollback

如需撤销，仅恢复物品清单价格绑定及对应文档；无需回滚数据。
