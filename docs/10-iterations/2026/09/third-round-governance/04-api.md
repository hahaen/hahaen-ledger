# API

现有路径和响应契约保持不变。服务端继续从 Sa-Token 当前用户推导账本，前端不新增 userId 权限参数。删除接口沿用既有路径，由 Service 同时恢复余额并写 `deleted` 审计。
