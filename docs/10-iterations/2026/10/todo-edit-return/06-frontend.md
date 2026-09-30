# 前端

ha-todo-editor 新增 returnAfterSave，成功跳过旧详情回到清单，由既有 onShow 刷新。取消、未保存确认、保存禁用、失败原幂等键重试保持原行为。使用 uni 导航，双端共用，无 wx 专用调用。
