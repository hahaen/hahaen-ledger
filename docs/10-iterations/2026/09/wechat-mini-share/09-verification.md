# 验证

| 范围 | 状态 | 证据/限制 |
| --- | --- | --- |
| 转发标题与路径 | PASS（静态设计） | 统一返回固定标题和 `/pages/index/index`，不带账单及用户数据。 |
| 微信条件编译 | PASS（代码/产物） | 注册函数中的 `onShareAppMessage` 位于 `MP-WEIXIN` 条件块内；微信产物包含 `utils/wechatShare.js`，全部 14 个页面均引用该模块。 |
| 全页面覆盖 | PASS（静态+产物） | 专测从 `pages.json` 读取 14 个页面逐一校验，15/15；产物页面引用清单为 14/14。 |
| 前端 Node 回归 | PARTIAL | 全量 75/77，2 项为工作区既有 mine 模板断言与 tap-feedback 标记不匹配。 |
| TypeScript | PASS | `pnpm run typecheck`，`vue-tsc --noEmit` exit 0。 |
| H5 生产构建 | PASS | `pnpm run build:h5` 输出 `DONE Build complete.`。 |
| 微信小程序生产构建 | PASS | `pnpm run build:mp-weixin` 输出可导入微信开发者工具的产物。 |
| Lint | BLOCKED | `pnpm run lint` 返回 `ERR_PNPM_NO_SCRIPT`，项目未配置 lint script。 |
| Git 空白检查 | PASS | `git diff --check` 无 whitespace error；仅有工作区既有 CRLF→LF warning。 |
| 微信开发者工具/真机右上角转发 | NOT_RUN | 需要导入 `app/dist/build/mp-weixin` 并使用真实小程序运行环境。 |
