# 设计

Java 雪花 ID 使用 64 位整数存储，但浏览器 JavaScript 的安全整数范围有限。所有面向前端的实体 ID 改为十进制字符串；数据库 Entity、Service 内部和 Controller 入参继续使用 `Long/long`，由 Spring/Jackson 在请求边界转换。

统一增加 `app/src/utils/id.ts` 的 `stringId`，用于路由参数和旧本地缓存值归一化。
