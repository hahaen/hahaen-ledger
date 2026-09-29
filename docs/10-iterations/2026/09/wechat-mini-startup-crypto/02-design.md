# Design

微信开发者工具实测 `app.js` 启动时 `Cannot read properties of undefined (reading 'crypto')`，随后的页面未注册是级联错误。源码静态导入 `node-forge`；依赖的 `lib/util.js` 通过 `self/window` 选择全局对象，在当前微信服务环境得到 undefined。

Vite 只对 `node-forge/lib/util.js` 的特定表达式定向替换为 `globalThis`，其余代码不变。若依赖升级导致原表达式消失，构建主动失败，避免静默失效。曾尝试延迟导入，但 uni-app 的小程序产物仍从启动公共包加载该依赖，故未采用。
