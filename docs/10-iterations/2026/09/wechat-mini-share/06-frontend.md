# 前端

- 新增 `app/src/utils/wechatShare.ts`，集中定义固定转发标题、首页路径和微信生命周期注册。
- `pages.json` 中全部 14 个页面调用 `registerWechatShare()`；认证、协议、首次使用和新增/编辑记账页也覆盖右上角转发。
- 所有页面的转发目标统一为首页，不复用当前路由或查询参数。
- H5 继续保留原有页面和逻辑；条件编译只在微信小程序端挂载 `onShareAppMessage`。
