# 测试

- 路由守卫与现有页面回归：`node --test tests/auth-guard.test.mjs tests/entry.test.mjs tests/page-flows.test.mjs tests/calendar-layout.test.mjs tests/account-layout.test.mjs`，28/28 通过。
- 前端类型检查：`pnpm exec vue-tsc --noEmit`，exit 0。
- H5 生产构建：`pnpm run build:h5`，exit 0。
- 微信小程序生产构建：`pnpm run build:mp-weixin`，exit 0。
- 真实 H5 登录、退出、浏览器前进/后退和直接地址访问：当前未执行，需有效会话和浏览器运行环境。
