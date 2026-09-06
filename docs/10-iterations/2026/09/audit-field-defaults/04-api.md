# API

核对 `auth/vo/LoginVO`、`auth/dto`、`account/dto`、`transaction/dto`，请求及登录响应未声明公共审计字段，无需新增或修改 DTO/VO 参数。账户/账单等直接返回 Entity 的接口沿用既有字段名；createdBy 现在允许返回 null，createdAt/deleted 正常创建仍自动填充。

未修改身份认证、用户/账本归属条件及写接口参数。HTTP 全量业务回归本次 NOT_RUN。
