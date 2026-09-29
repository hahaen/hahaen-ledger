# H5 与微信小程序页面适配复查

## Requirement

保持现有业务、接口、流程和薄荷绿视觉风格，仅修复页面布局、滚动、安全区、组件遮挡及跨端显示差异。覆盖首页、日历、资产、物品、我的、记账与常用二级页，并检查窄屏和高宽比变化。

## Design

以现有 `docs/08-design/README.md` 和 `app/src/prototype.scss` 为基准。优先修复公共页头及共享容器，再处理实测页面中的局部溢出；不引入新的视觉体系。

## Database

不涉及。

## API

不涉及。

## Backend

不涉及。

## Frontend

- `PageHeader.vue` 移除品牌页头额外的 20px 顶部偏移。微信开发者工具 iPhone 15 Pro Max 模拟器修复前品牌行与顶部系统区域相撞、首卡进入胶囊区域；修复后标题与胶囊同层，卡片从胶囊下方开始。H5 页头不受该偏移影响。
- `prototype.scss` 对高度不超过 740px 的日历页允许整页纵向滚动，并给当日记录区保留 200px。原 320×568 H5 中记录区高度仅约 2px；修改后可滚动至日期摘要与空态卡。高屏继续沿用既有独立记录滚动。
- `prototype.scss` 对高度不超过 500px 的记账页按视口高度缩短四行数字键盘，并收紧类型切换与金额卡片的纵向间距。667×375 H5 原先金额卡完全被键盘覆盖；修改后金额输入可见，表单保留独立滚动。
- 未触及页面脚本、API、路由、数据库与业务交互逻辑。

## Testing

使用本机已登录 H5 开发页 `http://localhost:5180/haji/` 和微信开发者工具 2.02.2608070 中已运行的 `app/dist/dev/mp-weixin`。仅作只读导航、滚动和打开/取消弹窗；未提交记账、物品、账户、资料或密码。H5 通过 Chrome 设备视口分别检查 320×568、375×667、390×844、430×932、667×375；微信模拟器检查 iPhone 5（320×568）与 iPhone 15 Pro Max（430×932）。

| 页面 | H5 | 微信开发者工具模拟器 | 结果与边界 |
| --- | --- | --- | --- |
| 首页 | PASS | PASS | 摘要卡、空态、浮动新增按钮、五项底栏与系统胶囊间距可见；窄屏无横向溢出。 |
| 日历 | PASS | PARTIAL | 320×568 H5 修复后整页可滚到日期记录；小程序 iPhone 5 用模拟器的页面向下操作可见记录空态，触屏惯性滚动与有数据长列表未验。 |
| 资产 | PASS | PASS（空数据） | 资产卡、账户分组、悬浮按钮与底栏可见；有账户长列表和账户详情未做运行态验证。 |
| 物品 | PASS | PASS | 摘要、筛选、在役/退役行与底栏显示；iPhone 5 新增物品弹窗的字段和双按钮完整可见，打开后取消。 |
| 我的 | PASS | PASS | 用户卡、菜单和底栏可见；个人中心在 iPhone 5 的表单与固定双按钮可见，未提交资料。 |
| 新增记账 | PASS | PASS（布局） | 320×568 H5 表单可在固定键盘后独立滚到后续字段；667×375 修复后金额可见；iPhone 5 小程序数字键盘与页头无横向溢出。未尝试保存。 |
| 物品详情 | PASS | PASS | 375×667 H5、iPhone 5 小程序的成本卡、资料与固定操作栏显示；只读打开已有物品。 |
| 帮助/个人中心 | PASS | PASS | 小屏帮助卡片正常换行；个人中心固定操作栏与表单未互相遮挡。 |

H5 的上述页面在检查视口中均为 `documentElement.scrollWidth === innerWidth`；矮屏日历改用整页滚动，高屏仍由记录区独立滚动，记账表单始终独立滚动。真机微信、iOS Safari、实际触摸滑动、键盘弹出后的视口变化、账单/账户有数据详情及全部业务写入为 NOT_RUN，不将模拟器或构建结果扩写为真机验收。

矮屏日历的 H5 画面：[320×568 全页截图](evidence/h5-calendar-320x568.jpg)、[向下滚动后的视口截图](evidence/h5-calendar-scrolled-320x568.jpg)。按 PageDown 后 `scrollY=312.5`，记录区顶部由约 572px 移至 260px，固定底栏顶部仍为 492px，空态卡完整可见。

## Commands

- `cd app && pnpm exec node --test tests/*.test.mjs`：87/87 PASS。
- `cd app && pnpm run typecheck`：exit 0。
- `cd app && pnpm run build:h5`：`DONE Build complete.`，仅代表 H5 产物构建。
- `cd app && pnpm run build:mp-weixin`：`DONE Build complete.`，仅代表小程序产物构建。
- 最终样式修改后再次执行 `pnpm run build:h5`、`pnpm run build:mp-weixin`：两端均输出 `DONE Build complete.`。
- `git diff --check`：exit 0。

## Verification

页面模拟器与浏览器结果见上表。前端回归、类型检查、双端构建、`git diff --check` 为 PASS；微信小程序日历触摸及有数据列表为 PARTIAL；真机、Safari、写操作为 NOT_RUN。没有后端、API 或数据库变更。

## Rollback

可按本轮样式和文档 diff 回退；不涉及数据迁移。
