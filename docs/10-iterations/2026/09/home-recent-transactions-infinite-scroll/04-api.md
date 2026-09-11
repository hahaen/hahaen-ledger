# API

新增 `GET /api/app/home/recent-transactions`，可选 `beforeMonth` 参数格式为 `YYYY-MM`。响应为 `startMonth`、`endMonth`、`transactions`、`hasMore`。`beforeMonth` 为排他上界；省略时首段为当前月和上月。

接口从当前 Sa-Token 用户取归属，忽略前端用户身份；不属于任何写操作，不要求幂等键。
