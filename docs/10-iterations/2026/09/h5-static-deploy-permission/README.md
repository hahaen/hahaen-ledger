# `h5-static-deploy-permission`｜H5 静态目录部署权限

## 摘要

修复 Jenkins 部署 H5 时，对 root 所有的 Nginx 静态目录执行 `install -d` 触发无权限 chmod、导致产物未复制的问题。

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
