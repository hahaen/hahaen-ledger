# 前端

- `AuthPage.vue` 增加 `agreementAccepted`；计算提交条件和 `submit()` 双重检查，避免仅靠按钮禁用。
- 协议链接通向 `pages/legal/agreement/agreement` 与 `pages/legal/privacy/privacy`；页面在 `pages.json` 注册。
- `LegalDocumentPage.vue` 通过 `legalDocuments.ts` 统一渲染用户协议和隐私协议，避免两页正文或样式分叉。
- `h5AuthGuard.ts` 将协议路由加入未登录白名单；业务路由保护不变。
- `styles.scss` 在关于与帮助既有令牌和内容卡基础上补充认证协议行与正文阅读样式。

