# 账户详情固定筛选与流水独立滚动

## 目标

资金账户和信贷账户进入详情后，账户顶部信息、账户流水标题及“全部/支出/收入/转账/还款”筛选按钮固定；日期、笔数和流水内容在下方独立滚动，日期分组标题按组吸顶并由下一组替换。

## 变更范围

仅修改 `app/src/pages/account/account.vue`、`app/src/prototype.scss` 和前端布局回归测试。数据库、API、后端、金额和账户筛选逻辑不变。

## 验收结论

- `frontend`：PASS，账户详情拆分为固定头部和独立流水 `scroll-view`。
- `testing`：见 `07-testing.md`。
- `verification`：见 `09-verification.md`；独立只读 H5 运行时滚动证据 PASS，PNG 截图和微信开发者工具人工验收仍为 NOT_RUN。

## 回滚

恢复账户详情原有普通流水容器，并移除本档案对应的账户 flex/scroll 样式即可，无数据库回滚操作。
