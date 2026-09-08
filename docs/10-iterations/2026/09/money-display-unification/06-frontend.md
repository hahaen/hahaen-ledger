# 前端

## 修改文件

- app/src/utils/money.ts
- app/src/pages/index/index.vue
- app/src/pages/assets/assets.vue
- app/src/pages/account/account.vue
- app/src/pages/detail/detail.vue
- app/src/pages/entry/entry.vue

- 修改公共金额工具，集中处理分值到人民币展示文本的转换。
- 迁移首页、资产页、账户详情页和账单详情页的大字号金额，移除模板中的 `.slice(1)`。
- 迁移退款金额占位符，避免直接使用 `/ 100` 和 `toFixed(2)` 生成展示文本。
- 账户详情编辑表单的初始金额也通过 `formatYuan()` 生成可编辑字符串，保存时仍由既有分值转换校验处理，不改变提交语义。
