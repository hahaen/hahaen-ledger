# Frontend

`app/vite.config.ts` 注册 `forge-wechat-global-scope` 预转换插件，仅匹配 `node-forge/lib/util.js` 原模块，跳过 Rollup CommonJS 代理模块。将其 `self/window` 全局对象选择表达式改为 `globalThis`。生成的小程序 `common/vendor.js` 已核对 `util.globalScope` 返回 `globalThis`。密码加密源码、微信安全随机数和 H5 页面均未修改。
