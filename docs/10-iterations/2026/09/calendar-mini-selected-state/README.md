# 日历微信小程序选中日期可见性

## 迭代摘要

微信小程序日历中的选中日期在 H5 可见、在小程序端不可见。根因风险集中在日期格及数字使用原生 `button`/`text` 的跨端渲染差异。本次只调整日历日期格的展示节点，保留月份切换、选中日期、当日标识、记账类型圆点、跨月数据和单日详情逻辑。

## 状态

- PASS：日期格模板和共享样式完成跨端调整。
- PASS：前端 53/53 回归、类型检查、H5/微信小程序生产构建和差异空白检查。
- PASS：小程序编译产物静态确认日期格与日期数字均为 `view`，选中态使用明确的 `#49ad9c` 实体背景色。
- BLOCKED / NOT_RUN：本次未在微信开发者工具中导入并人工点击日历；构建和产物静态检查不能替代真实小程序画面验收。

## 文件导航

- [01-requirement.md](01-requirement.md)
- [02-design.md](02-design.md)
- [03-database.md](03-database.md)
- [04-api.md](04-api.md)
- [05-backend.md](05-backend.md)
- [06-frontend.md](06-frontend.md)
- [07-testing.md](07-testing.md)
- [08-commands.md](08-commands.md)
- [09-verification.md](09-verification.md)
- [10-rollback.md](10-rollback.md)
