# `wechat-mini-share`｜微信小程序转发

日期：2026-09-20。为微信小程序全部页面接入右上角“转发”菜单，统一分享标题并回到首页；H5 不注册该生命周期，不新增后端、数据库或隐私数据接口。

## 状态

- 代码实现：已完成
- 转发专测：15/15 PASS，覆盖 `pages.json` 全部 14 个页面
- H5/微信小程序生产构建：PASS
- 全量前端回归：PARTIAL，77 项中 75 项通过，2 项为工作区既有 mine 模板断言与当前 tap-feedback 标记不匹配
- TypeScript：PASS
- Lint：BLOCKED，`app/package.json` 未配置 lint script
- 微信开发者工具/真机真实转发：待导入产物后验收

## 档案导航

见 `01-requirement.md` 至 `10-rollback.md`。
