# 测试

计划执行：

- 全局搜索金额字段、货币格式化、`toFixed`、`.slice(1)` 和 `/ 100` 残留。
- `pnpm run typecheck`。
- `pnpm run build:h5`。
- `pnpm run build:mp-weixin`。
- 对公共格式化函数执行边界样例检查：整数金额、有效小数、零值、空值、字符串和负数。
