# 后端

新增 auth/user/file 三个域的 Entity、Mapper、DTO/VO、Service 和 Controller。认证服务负责验证码、RSA 解密、BCrypt、Sa-Token 和登录日志；文件服务负责归属校验、对象 Key 生成、MinIO 预签名 URL、上传确认、预览和删除。

`TRANSACTION_ATTACHMENT` 当前明确返回业务未开放，不执行任何对象创建或数据库写入。
