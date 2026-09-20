# 全局按钮与可点击控件轻触反馈

日期：2026-09-20。范围：为 H5 与微信小程序的按钮和明确可点击控件增加轻微按压反馈；H5 在浏览器支持时调用 `navigator.vibrate(8)`，微信小程序调用 `uni.vibrateShort({ type: 'light' })`。不改变业务方法、接口、数据、导航和现有按钮视觉主题。

## 交付边界

- 共享 `prototype.scss` 为按钮、`role="button"`、`switch`、`picker`、链接和资产排序操作增加微小按压缩放/透明度反馈。
- H5 在 App 挂载时安装捕获式 `pointerdown` 监听，跳过禁用控件并抑制 40ms 内重复反馈。
- 微信小程序通过 App 外层捕获触摸事件识别按钮和 `data-tap-feedback` 控件，调用轻振动 API。
- 遵守 `prefers-reduced-motion`：H5 关闭振动和 CSS 过渡；现有记账键盘震动增加 API 存在性保护。

## 档案

- [requirement](01-requirement.md)
- [design](02-design.md)
- [database](03-database.md)
- [api](04-api.md)
- [backend](05-backend.md)
- [frontend](06-frontend.md)
- [testing](07-testing.md)
- [commands](08-commands.md)
- [verification](09-verification.md)
- [rollback](10-rollback.md)
