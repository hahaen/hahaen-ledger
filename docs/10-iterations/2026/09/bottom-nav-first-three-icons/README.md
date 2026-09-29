# 底部导航前三项图标放大

日期：2026-09-28

## Requirement

将首页、日历、资产图标放大到与物品、我的图标相协调；保留物品和我的原有样式。

## Design

首页、日历、资产字符图标使用与“我的”相同的 `scale(1.6)`。不改变物品描边图标、导航布局或选中背景。

## Database

NOT_RUN：不涉及数据库。

## API

NOT_RUN：不涉及 API。

## Backend

NOT_RUN：不涉及后端。

## Frontend

仅修改 `app/src/prototype.scss` 中首页、日历和资产图标缩放；同步前端规范。

## Testing

NOT_RUN：本次未运行测试、类型检查或构建。

## Commands

- 静态检索：确认三个图标使用 `scale(1.6)`，物品图标规则未改。

## Verification

- CSS 与规范静态核对：PASS。
- H5/微信实际页面视觉复核：NOT_RUN。
- 类型检查和构建：NOT_RUN。

## Rollback

移除 `.nav-icon-home`、`.nav-icon-calendar`、`.nav-icon-assets` 的 `scale(1.6)` 声明，并恢复相关文档即可。
