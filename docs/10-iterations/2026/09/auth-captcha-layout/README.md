# 登录与注册验证码布局修复

日期：2026-09-08。状态：PASS（前端修复与本地 H5 回归完成；真实后端联调仍受环境限制）。

## requirement

用户反馈 H5 登录、注册页验证码显示为细竖条。验收要求：图片完整可见，点击可刷新，窄屏不溢出。

## design

本地页面复现：验证码 SVG 已解码为 240×88，按钮实际宽度约 13.33px，图片宽度为 0。为共用验证码按钮明确设置占满网格列的宽度及居中布局，保留现有 96px / 82px 响应式列宽。

## database

本次仅修复前端样式，无数据库与 Flyway 变化。

## api

沿用 `/api/app/auth/captcha` 和现有 DTO；无协议变化。

## backend

沿用现有验证码生成与校验，无后端改动。

## frontend

登录、注册均使用 `AuthPage.vue`，修改 `prototype.scss` 的 `.captcha-button`，限定影响范围。

## testing

已执行登录/注册图片显示、点击刷新、320/375/414px 窄屏布局检查、类型检查及 H5/微信构建。

## commands

已读取项目规则、README、前端和设计规范、认证组件及相关样式；已通过本地浏览器测量复现图片宽度为 0。

## verification

- PASS：修复后登录页按钮宽 96px、图片宽约 82.7px，SVG naturalWidth=240，点击刷新会更换图片。
- PASS：注册页点击刷新会更换图片；320px 下按钮宽 82px、图片宽约 68.7px，375/414px 下页面宽度分别等于 viewport，无横向溢出。
- PASS：`pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin` 均 exit 0。
- BLOCKED：真实后端验证码接口和微信开发者工具未在本轮联调。

## rollback

未发布时仅撤回本迭代针对 `.captcha-button` 的样式修改；已部署时恢复上一版前端产物。不涉及数据回滚，保留其他工作区修改。
