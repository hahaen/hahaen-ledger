# `jenkins-compose-deployment`｜Jenkins 与 Docker Compose 部署脚本

## 摘要

为同仓库的 `server/` 后端和 `app/` 前端分别提供 Jenkins 流水线，并通过 SSH 让 Jenkins 容器调用宿主机 Docker Compose；不把 Docker Socket 暴露给 Jenkins 容器。

## 导航

- [需求](01-requirement.md)
- [设计](02-design.md)
- [数据库](03-database.md)
- [接口](04-api.md)
- [后端](05-backend.md)
- [前端](06-frontend.md)
- [测试](07-testing.md)
- [命令](08-commands.md)
- [验证](09-verification.md)
- [回滚](10-rollback.md)
