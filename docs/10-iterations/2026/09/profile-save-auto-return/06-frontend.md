# 前端

`pages/profile/profile.vue` 使用项目统一的圆角成功弹层，并移除“返回我的”按钮和额外的系统 Toast。保存成功后设置 0.5 秒定时器，定时器到期自动切换到“我的”页；卸载时通过 `clearTimeout` 清理未完成的定时器。
