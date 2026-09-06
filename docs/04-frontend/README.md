# 04｜前端规范

本目录是 Vue 3 + TypeScript + uni-app 的页面与交互约束入口，重点保证 H5 和微信小程序共用业务逻辑，同时保留清晰的平台差异边界。

## 代码位置

- 页面放在 `app/src/pages`，跨页状态放在 `app/src/stores`，复用 UI 放在 `app/src/components`。
- 页面只能通过 `app/src/utils/api.ts` 调用后端，不直接拼接请求或散落平台 SDK。
- 微信差异放在条件编译或工具层；首版底部导航只能是首页、日历、资产、我的。

## 交互验收

保存类按钮必须有 loading 和禁用态，成功后刷新相关状态，失败后给出可重试反馈。金额、日期和长度约束必须与后端一致。生产构建不允许通过大面积 `any`、关闭 strict、`eslint-disable` 或 `@ts-ignore` 掩盖类型问题。
