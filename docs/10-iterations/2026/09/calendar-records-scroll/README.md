# 日历页固定上半部分与账单记录独立滚动

## 目标

日历页上半部分（页头、月历、当日汇总和日期标题）固定在视口内，页面滑动手势只滚动选中日期的记账记录区域。

## 变更范围

仅修改 `app/src/pages/calendar/calendar.vue` 和 `app/src/prototype.scss`。数据库、API、后端、认证、金额口径和底部导航不变。

## 验收结论

- `frontend`：PASS（账单记录改为独立 `scroll-view`，日历页使用纵向 flex 布局并隐藏页面溢出）。
- `testing`：见 `07-testing.md`。
- `verification`：见 `09-verification.md`；独立只读 H5 运行时滚动证据 PASS，PNG 截图和真实微信开发者工具人工验收仍为 NOT_RUN。

## 回滚

恢复日历页账单区域原有 `view` 结构及本档案对应的日历 flex/scroll 样式即可，无数据库回滚操作。
