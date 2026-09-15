# 01｜系统架构

本目录说明哈记账的模块边界、调用方向和职责分工，用来约束代码应该放在哪里，以及哪些职责不能跨层泄漏。

## 后端边界

后端当前按 `auth`、`user`、`file`、`account`、`transaction`、`asset`、`home`、`calendar` 分域，另有 `common` 基础设施。当前没有 `book` 包，也没有 `app_book` 表；“单账本”是产品边界，不是当前可查询的独立账本实体。Controller 只做协议适配和参数接收，Service 负责业务规则、事务和归属校验，Mapper 只负责数据访问；金额、删除、幂等和权限不能下沉为“碰巧能用”的 Controller 或 SQL 行为。

认证需要区分两条路径：H5 由 `AuthController` 提供账号/密码/验证码接口；小程序由 `ledger.login()` 调用 `uni.login` 获取一次性 code，再请求 `/api/app/auth/wechat-mini/login`。后端服务端调用微信 code2Session，按 `(provider, open_id)` 查找或创建 `app_user`，最后复用 Sa-Token 签发业务 Token；`session_key` 只在服务端内存中短暂使用。

## 前端边界

`app/src/pages` 放页面，`components` 放复用组件，`stores` 放跨页状态，`utils/api.ts` 是唯一 API 入口，`utils/file.ts` 封装头像文件链路。微信平台差异应集中在条件编译或工具层，业务页面不能散落 `wx.*` 调用；当前代码使用 uni-app 的 `uni.login`/条件编译。

## 阅读重点

设计新域时，先确认它属于哪个业务边界，再同时设计数据、服务、API、前端状态和测试入口。任何跨域查询都必须说明归属条件、事务边界、异常传播方式和日志脱敏策略。
