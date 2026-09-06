# 前端环境配置

所有前端环境变量集中在本目录，dev/prod 对应 Vite 的 development/production mode。

| 文件 | 用途 |
| --- | --- |
| `.env` | dev/prod 共用的变量 |
| `.env.development` | dev 开发环境，默认本机 API |
| `.env.production` | prod 生产环境，必须填写实际 HTTPS API 地址 |
| `.env.local`、`.env.[mode].local` | 可选的本机覆盖配置 |
| `*.example` | 可提交的配置模板，Vite 不自动加载 |

首次使用在 app 目录执行（已有配置时不要覆盖）：

```powershell
Copy-Item env/.env.example env/.env
Copy-Item env/.env.development.example env/.env.development
Copy-Item env/.env.production.example env/.env.production
```

`pnpm run dev:h5` 和 `pnpm run dev:mp-weixin` 加载通用及 development 配置；`pnpm run build:h5` 和 `pnpm run build:mp-weixin` 加载通用及 production 配置。修改后重启开发服务或重新构建。

覆盖优先级从低到高：`.env` → `.env.local` → `.env.[mode]` → `.env.[mode].local` → 启动进程已有环境变量。旧 app 根目录的 `.env*` 内容需移到本目录。

实际环境文件均被 Git 忽略，模板同步新增变量。`VITE_*` 会进入客户端产物，只能放公开配置，不得放服务端密钥。
