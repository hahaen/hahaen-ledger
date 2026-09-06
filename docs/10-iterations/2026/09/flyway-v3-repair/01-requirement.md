# 需求

## 问题

DEV 启动时 Flyway 校验发现版本 3（`standardize audit columns and comments`）迁移失败，导致 Spring ApplicationContext 无法初始化。

## 目标

确认 V3 脚本与目标数据库实际结构的一致性；在不修改历史迁移文件、不丢失业务数据的前提下修复数据库迁移状态，并验证应用启动链路。

## 非目标

不关闭 Flyway 校验，不删除业务表，不重建数据库，不修改已经执行的 V1/V2/V3 历史脚本。
