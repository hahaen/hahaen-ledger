# testing

## 用例

1. 编辑态类型为 `INCOME` 时调用 `setType('EXPENSE')`，类型仍为 `INCOME`。
2. 编辑页源码中类型切换器带有新增态条件，避免编辑态渲染。
3. 新增态 `setType('TRANSFER')` 仍可切换。
4. 编辑态类型标签由回填类型生成“收入”等正确文案。
5. 修改内容后返回显示自定义放弃确认弹窗，继续编辑可关闭弹窗，确认放弃可离开页面。

实际结果在 `08-commands.md` 和 `09-verification.md` 中补充。
