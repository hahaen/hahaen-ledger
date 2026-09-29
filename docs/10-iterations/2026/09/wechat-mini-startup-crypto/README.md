# 微信小程序启动白屏修复

## Requirement

微信开发者工具编译成功后应显示首页，而不是空白页面；个人中心密码仍须使用 RSA-OAEP/SHA-256 加密。

## 复现证据

2026-09-28 在微信开发者工具 Stable 2.02.2608070，导入 `app/dist/dev/mp-weixin` 后，模拟器只有底部导航。Console 首个异常为 `app.js错误: Cannot read properties of undefined (reading 'crypto')`，随后出现 `Page "pages/index/index" has not been registered yet`。堆栈指向主包启动期，源码中 `passwordCryptoMp.ts` 顶层静态导入 `node-forge`，其生成的 `common/vendor.js` 在加载阶段读取运行环境的 `crypto`。

## Design

在 Vite 转换 `node-forge/lib/util.js` 时，将依赖中通过 `self/window` 选择全局对象的表达式定向替换为 `globalThis`。微信服务上下文提供 `globalThis`；现有 `self/window` 表达式可能返回 undefined，使依赖初始化阶段读取 `crypto` 时崩溃。插件要求原表达式存在，依赖版本改变时显式失败以便重新核对。保留微信安全随机数、RSA-OAEP/SHA-256 和 H5 路径。不得通过明文密码或弱随机数绕过启动错误。

## Database / API / Backend

本次无数据库、Flyway、API 或后端契约变更。

## Frontend / Testing / Commands / Verification / Rollback

代码仅修改 `app/vite.config.ts` 的定向转换插件，不改变加密模块的业务调用。定向互操作测试 2/2、全量前端回归 82/82、TypeScript、H5 和微信构建通过；开发构建在微信开发者工具 2.02.2608070 重新加载后首页可见。真机及真实设置/修改密码流程未执行。详见本目录 01–10 文件。
