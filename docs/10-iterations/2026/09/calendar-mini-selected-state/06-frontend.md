# 前端

## 修改

- `app/src/pages/calendar/calendar.vue`：日期格从原生 `button` 改为 `view`，日期数字从 `text` 改为 `view`；当前月日期继续选择并加载详情，非本月日期保持不可选。
- `app/src/prototype.scss`：`.calendar-cell` 使用明确的纵向 flex 布局；`.day-num` 使用明确的 flex 盒模型；选中态强制写入 `#49ad9c`、`background-color` 和白色文字，避免小程序显示为白色。
- `app/tests/calendar-layout.test.mjs`：增加跨端节点和样式回归断言。

## 保持不变

月份标题、前后月、今天、选中日期状态、今天状态、类型圆点、当日汇总、记录滚动区域、接口调用和底部导航均未改动。
