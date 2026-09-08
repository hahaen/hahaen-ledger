# 验证

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 公共金额组件 | app/src/components/MoneyDisplay.vue 统一渲染数值和右侧 ¥ | PASS |
| 首页/日历 | 组件覆盖摘要、日期收支和日汇总；视觉服务核对 | PASS |
| 资产/账户 | 组件覆盖净资产、账户余额、欠款、额度和还款账户金额；320px 截图确认不换行 | PASS |
| 账单/退款 | 组件覆盖详情金额、退款状态、退款记录和确认说明 | PASS（静态核对） |
| 记账输入 | 输入值与 ¥ 同行且 ¥ 在右侧；未改变提交值 | PASS |
| 320/375/414px | 只读视觉服务计算 scrollWidth === innerWidth，金额值右边界小于等于货币符号左边界 | PASS |
| 前端类型检查 | pnpm run typecheck exit 0 | PASS |
| H5 生产构建 | pnpm run build:h5 完成 | PASS |
| 微信小程序生产构建 | pnpm run build:mp-weixin 完成 | PASS（仅构建） |
| 真实登录态/微信工具 | 当前未取得真实会话和微信开发者工具 | NOT_RUN |
| 静态原型目录 | 项目规范定义为独立设计参考，不属于 app 构建入口，本迭代未修改 | NOT_RUN（范围外） |
