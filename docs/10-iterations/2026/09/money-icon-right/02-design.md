# 设计

- 新增 app/src/components/MoneyDisplay.vue，统一渲染可复用的“数值 + ¥”。
- 使用 prefix 支持收入、支出、转账等既有正负/文字前缀；金额格式继续复用 formatYuan()。
- 输入框中的货币符号保持在输入值右侧，并通过公共样式校正间距和对齐。
- MoneyDisplay 使用 flex 容器避免 text 嵌套在 320px 下把 ¥ 换行。
