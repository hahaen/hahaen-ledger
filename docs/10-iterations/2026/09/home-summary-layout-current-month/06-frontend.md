# frontend

修改 `app/src/prototype.scss`：增加 `.home-page .summary-amount { display:flex; width:100%; }`，解决 `.money-display { display:inline-flex; }` 覆盖首页金额块级布局的问题；将摘要卡底部字段改为按内容宽度排列，使用 32px 间距，并限制每列最大宽度，避免长的“本月支出”金额挤压“本月收入”。

首页 `app/src/pages/index/index.vue` 已使用 `localDateTime().slice(0, 7)` 初始化并在每次加载时刷新月份，再传递给 `ledger.refresh(month)`，因此 `2026 年 9 月` 只是当前业务日期落在 2026 年 9 月时的动态结果，不是写死文案。
