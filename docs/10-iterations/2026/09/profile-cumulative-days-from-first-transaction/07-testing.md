# 测试

`ProfileServiceTest` 覆盖：

- 最早记账日为当天、昨天的含首日自然日计算。
- 无最早记账日和未来最早记账日返回 0。
- `currentProfile()` 使用当前用户最早有效账单时间，而非账号 `created_at`。
- 当前用户无有效账单时个人资料返回 0。

完整后端回归、前端类型检查与真实登录态/API 联调状态见 [09-verification.md](09-verification.md)。
