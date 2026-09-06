# 后端

本次不修改 Entity、DTO、VO、Mapper、Service、Controller、事务、权限、日志或幂等代码。后续实现时，`asset_account` 应作为主表映射完整审计基类，主动删除必须写入删除审计字段；金额和交易/还款余额影响必须在同一事务中处理。

状态：NOT_RUN（不在本次数据库设计范围内）。
