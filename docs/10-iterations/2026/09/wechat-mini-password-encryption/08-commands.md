# Commands

- `node --test tests/password-crypto.test.mjs`：PASS，2/2。
- `node --test tests/*.test.mjs`：PARTIAL，75/77；2 项失败均为当前 mine 模板与既有断言不匹配。
- `pnpm run typecheck`：PASS。
- `pnpm run build:mp-weixin`：PASS。
- `pnpm run build:h5`：PASS。
- 小程序/H5 构建产物静态搜索：PASS；小程序存在兼容模块静态调用，H5 无 node-forge 特征代码。
- `mvn test`（`server/`）：PASS，53/53。
- `pnpm run lint`：BLOCKED，项目未配置该 script，未执行。
