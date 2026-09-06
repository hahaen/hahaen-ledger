# `app-environments`｜前端多环境配置

## 迭代目的

本迭代关注 H5/微信前端在 development、production 等环境下如何选择 API 地址和构建参数，并确保真实配置留在本地忽略文件中。

## 应记录的重点

- `.env.example`、`.env.development.example`、`.env.production.example` 的职责差异。
- `VITE_API_BASE_URL` 的来源、生产 HTTPS 要求和缺失时的保护性失败。
- H5 与小程序构建命令对环境变量的实际读取结果。
- 凭证不提交、不写日志、不写入文档示例。

## 当前档案状态

当前工作区未保留本迭代的 01–10 明细文件；此 README 只提供背景和审查范围，不能替代真实构建输出。
