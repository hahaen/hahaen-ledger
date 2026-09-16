# verification

- 新增态退出行为：PASS，输入内容和切换类型后不打开“放弃修改”弹层。
- 编辑态退出保护：PASS，修改既有账单回填内容后仍打开自定义确认弹层。
- 前端 Node 回归：PASS，55/55。
- TypeScript 类型检查：PASS，`pnpm run typecheck` exit 0。
- H5 生产构建：PASS，临时本地 API 地址下输出 `DONE Build complete.`。
- 微信小程序生产构建：PASS，临时本地 API 地址下输出 `DONE Build complete.`。
- 真实 H5/微信页面退出操作：NOT_RUN，未取得真实登录态；构建与静态测试不替代运行时验收。
