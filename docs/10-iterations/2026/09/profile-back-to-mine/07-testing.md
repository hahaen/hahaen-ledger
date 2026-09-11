# 测试

新增页面流程回归，调用个人中心返回函数并断言仅执行 `uni.switchTab({ url: '/pages/mine/mine' })`，同时确认页面不再引用通用 `backToLedger()`。
