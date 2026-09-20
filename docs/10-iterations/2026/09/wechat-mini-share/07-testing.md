# 测试

新增 `app/tests/wechat-share.test.mjs`，静态检查统一标题、首页路径、微信生命周期注册以及八个业务页的注册调用。

实际结果：转发专测 9/9 PASS；全量 `tests/*.test.mjs` 为 69 项中 67 项通过，2 项失败来自工作区既有 `mine` 模板断言与当前 `tap-feedback` 标记不匹配；类型检查 PASS。H5 与微信小程序生产构建均 PASS。真实微信转发仍需微信开发者工具或真机验收。
