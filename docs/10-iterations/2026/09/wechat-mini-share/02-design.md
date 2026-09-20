# 设计

新增 `app/src/utils/wechatShare.ts` 作为唯一转发内容定义与注册入口。`registerWechatShare()` 内部使用 `MP-WEIXIN` 条件编译调用 uni-app 的 `onShareAppMessage`，各业务页仅调用一次该注册函数。

转发统一返回 `title: 哈记账｜简单记账，安心生活` 和 `path: /pages/index/index`。统一回到首页可以避免把账单详情、账户详情和当前用户数据编码进可被转发的路径，也不要求接收方拥有原发送方的会话。

本次不使用远程图片作为 `imageUrl`，避免把静态资源域名、下载域名和分享链路绑定到转发功能。
