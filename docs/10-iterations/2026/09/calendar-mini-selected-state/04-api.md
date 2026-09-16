# API

## 状态

NOT_APPLICABLE：本次不改变任何接口协议。

## 保持不变的调用

- 月历：`GET /api/app/calendar`。
- 单日详情：`GET /api/app/calendar/{date}`。
- 月度摘要仍由既有 `ledger.refresh(monthKey)` 负责。

请求参数、响应结构、认证、权限、幂等和错误处理均未修改。
