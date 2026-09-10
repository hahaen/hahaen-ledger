# testing

- 前端类型检查：PASS（`pnpm run typecheck`，exit 0）。
- H5 生产构建：PASS（临时注入 `VITE_API_BASE_URL=http://127.0.0.1:8080` 后执行 `pnpm run build:h5`）。
- 只读视觉夹具：PASS（`http://127.0.0.1:18761/#/pages/detail/detail?id=2` 与新增记账页均显示紧凑页头）。
- 有效登录态下的账单详情视觉核对：BLOCKED；原本地开发地址被认证页拦截，未获得可用会话。
