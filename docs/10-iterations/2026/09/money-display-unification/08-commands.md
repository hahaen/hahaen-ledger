# 命令

- app：pnpm run typecheck，exit 0。
- 未设置生产 API 地址时执行 pnpm run build:h5 和 pnpm run build:mp-weixin，均按项目配置主动失败，原因是缺少 VITE_API_BASE_URL；记录为环境前置条件，不作为代码失败。
- 临时设置 VITE_API_BASE_URL=http://127.0.0.1:8080 后执行 pnpm run build:h5，输出 DONE Build complete.，exit 0。
- 临时设置 VITE_API_BASE_URL=http://127.0.0.1:8080 后执行 pnpm run build:mp-weixin，输出 DONE Build complete.，exit 0。
- 使用 TypeScript transpileModule 加载 src/utils/money.ts 执行边界样例，结果与预期一致。
- 使用 rg 全局复查金额格式化残留；运行时页面未发现 toFixed、.slice(1)、手写 .00 或 /100 展示逻辑。
