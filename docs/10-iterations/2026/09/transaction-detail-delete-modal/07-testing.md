# testing

- 前端类型检查：PASS（`pnpm run typecheck`，exit 0）。
- H5 生产构建：PASS（临时注入本地 `VITE_API_BASE_URL` 后执行 `pnpm run build:h5`）。
- 微信小程序生产构建：PASS（临时注入本地 `VITE_API_BASE_URL` 后执行 `pnpm run build:mp-weixin`）。
- 只读视觉夹具：PASS（账单详情删除弹层打开、文案和取消关闭已核对；未执行真实删除）。
- 有效登录态下的真机/浏览器视觉验收：NOT_RUN。
