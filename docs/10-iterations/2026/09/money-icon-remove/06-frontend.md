# 前端

- app/src/components/MoneyDisplay.vue：移除钱币图标节点。
- app/src/components/TransactionRow.vue：无障碍金额文本改为数值加元文字。
- app/src/pages/index/index.vue、calendar/calendar.vue、assets/assets.vue、account/account.vue、detail/detail.vue、entry/entry.vue：统一使用无图标金额展示。
- assets.vue、account.vue、entry.vue：移除输入框中的 ¥。
- app/src/utils/money.ts：移除未使用的带符号格式化入口。
- app/src/prototype.scss：清理货币图标样式、间距和输入宽度补偿。
