# 前端

- `Account`、`Transaction`、`Refund`、登录/个人资料/文件响应中的实体 ID 改为 `string`。
- 账户页、账单详情页、记账编辑页不再使用 `Number(query.id)`，统一通过 `stringId` 读取路由参数。
- 账户排序的 ID 兜底比较改用字符串比较；旧的本地账户缓存同时兼容数字并转为字符串。
