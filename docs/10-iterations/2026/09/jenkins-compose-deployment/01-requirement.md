# 需求

## 来源

用户要求将 Gitee、Jenkins 与已有 Docker 服务连接，实现前后端分离的自动化部署。仓库为单一仓库，包含 `server/` 和 `app/`。

## 范围

- 后端通过 `server/Jenkinsfile` 部署到 `/home/hahaen/hahaen-ledger`。
- 前端通过 `app/Jenkinsfile` 构建 H5 并发布到 Nginx 静态目录。
- 生产密钥只由服务器 `/home/hahaen/haji-prod.env` 提供。

## 非目标

- 不修改业务代码、数据库 Migration 或真实服务器密钥。
- 不在 Jenkins 容器中安装 Docker 客户端或挂载 Docker Socket。
