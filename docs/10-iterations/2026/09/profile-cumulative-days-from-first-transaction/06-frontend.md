# 前端

`app/src/pages/mine/mine.vue` 已通过 `profile.cumulativeDays` 展示服务端值，展示结构和 API 调用无需修改。无有效账单时服务端返回 0，现有 `|| 0` 兜底与该语义一致。

不在前端查询账单或根据账号创建时间计算，避免与后端用户隔离和逻辑删除规则分叉。
