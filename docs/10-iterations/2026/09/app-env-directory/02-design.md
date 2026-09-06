# 设计

保留 development/production mode 分别对应 dev/prod。Vite 的 envDir 和 loadEnv 使用相对于配置文件解析的同一绝对目录。通用配置使用 .env，环境差异使用 .env.development/.env.production；仓库提供 example，本地实际配置忽略。现有变量均为环境差异，不引入无业务用途的通用变量。
