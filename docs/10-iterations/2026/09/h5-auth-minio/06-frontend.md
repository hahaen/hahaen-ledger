# 前端

H5 登录/注册页面接入 `/password-key`、`/captcha`、`/h5/register` 和 `/h5/login`，密码不以明文请求体发送；提交按钮有禁用和 loading 状态，认证失败自动刷新验证码。

新增 `utils/file.ts` 封装头像上传：请求上传授权、使用浏览器 PUT 直传、确认文件和请求预览地址。页面不直接访问 MinIO SDK 或密钥。
