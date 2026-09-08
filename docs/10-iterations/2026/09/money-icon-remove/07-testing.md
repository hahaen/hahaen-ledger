# 测试

- 静态检索运行时 app/src，确认无 ¥、￥、money-currency、currency 类金额图标引用。
- 检查 MoneyDisplay 的整数、小数、正负号、文字前缀和长金额格式仍由原有逻辑输出。
- 检查金额输入框不再因图标节点保留空白或缩窄。
- 执行 TypeScript 类型检查和 H5、微信小程序构建。
