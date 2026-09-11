# 首页重复加载请求修复

## requirement / design

H5 已登录首页刷新时，应用启动和页面 `onShow` 都调用 `ledger.refresh()`；从日历快速切回首页时，日历与首页也会同时刷新同月数据，导致账户和摘要接口各请求两次。启动阶段仅恢复会话和处理根路径跳转；Store 合并同月进行中的刷新，首页复用同月摘要。

## database / api / backend

无变更。

## frontend

移除 H5 `App.vue` 的启动预取；首页自动显示时复用 Store 同月摘要，未命中时才调用 `ledger.refresh()`。Store 对相同月份的并发刷新返回同一 Promise。根路径仍会跳转首页，401 仍由 API 统一处理器负责清理会话与跳转登录页。

## testing / commands / verification

PASS：`pnpm run typecheck`、临时注入本地 API 地址后的 `pnpm run build:h5` 均退出 0；`node --test tests/page-flows.test.mjs` 为 17/17 通过。静态调用点与回归测试显示 H5 启动路径不再调用 `ledger.refresh()`，同月并发刷新已合并；有效登录态的浏览器 Network 面板验收未执行。

## rollback

恢复启动预取即可，但会恢复重复请求。
