# 前端规范

首版底部导航固定为：首页、日历、资产、我的。页面仅通过 `src/utils/api.ts` 调用服务端；金额使用 `utils/money.ts`；跨页数据放 `stores/ledger.ts`。异步页面需要加载、空、错误、重试、提交中和成功刷新状态。微信能力使用 uni-app/条件编译封装，保持 H5 复用。

## 环境配置

前端使用 Vite `development`/`production` mode 分别对应 dev/prod。环境文件统一放在 `app/env/`，Vite `envDir` 与 `loadEnv` 均指向此目录。复制其中的 `.env.example`、`.env.development.example`、`.env.production.example` 为去掉 `.example` 后缀的实际文件：`.env` 放通用变量，环境文件放差异变量。开发 API 默认 `http://127.0.0.1:8080`；生产 API 默认为空，构建前必须配置 `VITE_API_BASE_URL`。实际环境文件已纳入 Git 忽略，不能提交凭证、Token 或永久签名 URL。

加载优先级：进程已有环境变量 > `.env.[mode].local` > `.env.[mode]` > `.env.local` > `.env`。旧 app 根目录配置需迁入 `app/env/`；修改后重启开发服务。客户端通过 `utils/env.ts` 集中读取运行配置。

## 平台运行时与启动验收

H5 和微信平台必须分别显式依赖 @dcloudio/uni-h5、@dcloudio/uni-mp-weixin，并与 uni-app 编译器版本一致。由平台插件配置 Vue 运行时及挂载，禁止使用空生命周期 shim 替代。业务 ref/reactive/computed 从 vue 导入；类型扩展采用模块扩展，不能重声明 Vue 模块覆盖官方类型。已有 BottomNav 自定义导航，挂载时通过 uni.hideTabBar 隐藏平台导航。

启动后必须实际访问页面并验证首次使用、进入首页与底部导航；不能只依据 Vite ready 或构建退出码判断页面可用。
