# API

本次不新增或修改 API、DTO、VO、错误码、权限接口或幂等协议。后续账户 API 必须通过 `utils/api.ts` 对接，并由服务端按当前 Sa-Token 用户和 `deleted=0` 过滤，不能信任前端传入的 `userId`；账户名称允许重复，接口应使用账户ID作为唯一定位。

状态：NOT_RUN（不在本次数据库设计范围内）。
