# 测试

更新 `page-flows.test.mjs`：模拟头像上传成功后，断言页面立即使用返回的预览 URL、仍保持未配置状态、已暂存文件 ID，并且资料请求列表为空；随后点击保存才断言资料 PUT 含该 `avatarFileId`。
