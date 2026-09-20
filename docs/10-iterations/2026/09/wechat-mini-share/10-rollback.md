# 回滚

如需回滚，仅移除本迭代新增的 `app/src/utils/wechatShare.ts`、`app/tests/wechat-share.test.mjs`、各业务页对应的注册调用和本迭代文档；同时从 `page-flows.test.mjs` 移除本迭代为测试夹具增加的 `defaultWechatShare` mock。不回滚或覆盖工作区中其他未提交改动。回滚后重新执行前端回归、类型检查和双端构建。
