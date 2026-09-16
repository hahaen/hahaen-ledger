# 设计

## 根因判断

日期格原先使用原生 `button`，日期数字使用 `text`，而选中态背景和文字颜色依赖子节点盒模型。H5 与微信小程序对原生按钮默认外观、`text` 的布局和背景渲染存在平台差异，导致小程序端存在选中态不可见风险。

## 方案

- 日期格改为 `view`，使用 `role="button"` 和 `aria-*` 属性保留语义。
- 日期数字改为 `view.day-num`，用明确的 `display:flex`、宽高和居中规则承载背景与文字。
- 非本月日期通过事件表达式阻止点击，保留灰态；当前月日期继续调用既有 `selectDay`。
- 选中态继续复用 `.calendar-cell.selected .day-num`，但使用 `#49ad9c`、`background-color` 和白色文字的 `!important` 明确覆盖，避免微信小程序端 CSS 变量或层叠失败后回退为白色。

## 影响

仅影响 `app/src/pages/calendar/calendar.vue`、`app/src/prototype.scss` 和日历布局测试；数据库、API、后端、状态请求和业务规则不变。
