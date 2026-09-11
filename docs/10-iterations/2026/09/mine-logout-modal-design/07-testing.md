# 测试

- 页面回归：`node --test tests/page-flows.test.mjs` 通过 19/19。新增用例覆盖不再调用 `uni.showModal`、自定义弹层存在、确认时重复点击只发起一次注销以及完成后关闭弹层。
- 类型检查：`pnpm run typecheck` 通过。
- H5 生产构建：临时注入本地 API 地址后 `pnpm run build:h5` 通过。
- 微信小程序生产构建：临时注入本地 API 地址后 `pnpm run build:mp-weixin` 通过。
- 运行时视觉夹具：启动本地只读夹具后，内置浏览器访问被 `ERR_BLOCKED_BY_CLIENT` 拦截，未生成截图。
