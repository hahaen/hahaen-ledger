# 实际命令与结果

1. `pnpm run dev:h5 -- --port 5174`：启动成功；修复前复现空白，最终停止辅助 5174 服务。
2. `Invoke-WebRequest http://localhost:5173/src/main.ts`：修复前只含 createApp 定义，无平台挂载。
3. `pnpm view @dcloudio/uni-h5@3.0.0-4080420251103001 dependencies --json` 和微信同版本查询：版本依赖核对成功。
4. `pnpm add -E @dcloudio/uni-h5@3.0.0-4080420251103001 @dcloudio/uni-mp-weixin@3.0.0-4080420251103001`：exit 0。
5. `pnpm remove @vue/reactivity @vue/runtime-core @vue/runtime-dom @vue/shared`：exit 0。
6. 重启 `pnpm run dev:h5`：最终 localhost:5173 可访问，平台路由和首屏恢复；重启过程中辅助进程曾因端口占用自动使用 5174，已停止。
7. `pnpm run build:h5`：exit 1，生产 API 缺失校验按预期阻止构建。
8. `$env:VITE_API_BASE_URL = 'http://127.0.0.1:8080'; pnpm run build:h5`：exit 0，最终含导航修复的 H5 构建通过。
9. 同一临时变量下 `pnpm run build:mp-weixin`：exit 0，9 个页面 WXML 与 app.json 存在。
10. `pnpm run typecheck`：增加 onMounted 后暴露既有 Vue 类型覆盖问题；移除覆盖声明并增加正确模块扩展后最终 exit 0。

构建仍提示 Sass legacy-js-api 弃用，微信构建提示既有 h1 标签选择器兼容问题；不影响本次构建完成。

