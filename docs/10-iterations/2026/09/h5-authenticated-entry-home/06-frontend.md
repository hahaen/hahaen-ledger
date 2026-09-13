# 前端

- `app/src/utils/h5AuthGuard.ts` 新增已认证默认入口判断：根路径、登录页、注册页需要进入首页。
- `app/src/App.vue` 在恢复本地 token 后使用该判断执行首页跳转。
- `app/tests/auth-guard.test.mjs` 覆盖默认认证入口回首页，以及业务页和协议页不被改写的边界。
