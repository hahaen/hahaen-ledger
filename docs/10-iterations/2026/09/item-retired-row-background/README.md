# 物品清单退役行灰色底色

## Requirement

物品清单中，状态为“已退役”的物品行使用灰色底色；在役物品仍使用白色底色。全部和已退役筛选下按同一状态规则展示。

## Design

直接依据列表项的 `status === 'RETIRED'` 添加行状态类，灰色只作用于对应物品行，保留现有列表圆角、分隔线和点击区域。

## Database

不涉及数据库、Migration 或数据迁移。

## API

沿用现有 `status` 字段，不修改请求、响应或权限。

## Backend

不修改后端。

## Frontend

`items.vue` 根据物品 `status` 为退役行添加 `item-row-retired`；`prototype.scss` 将该行底色设为 `#f1f3f2`。在役行继续使用列表的白色底色。

## Testing

静态检查确认条件类仅由 `RETIRED` 状态触发，样式已进入 H5 CSS 和微信小程序 WXSS。类型检查和两端生产构建通过。真实 H5 列表视觉、微信开发者工具和真机视觉未执行。

## Commands

在 `app/` 执行 `pnpm run typecheck`、`pnpm run build:h5`、`pnpm run build:mp-weixin`，均退出码 0。仓库根目录执行 `git diff --check`，退出码 0；使用 `rg -l 'item-row-retired' dist/build/h5 dist/build/mp-weixin` 确认两端产物均包含状态类与样式。

## Verification

| 项目 | 状态 | 证据/限制 |
| --- | --- | --- |
| 退役行灰底、在役行白底的条件实现 | PASS（静态） | 模板按 `RETIRED` 添加类；退役样式为 `#f1f3f2`。 |
| 类型检查与 H5/微信构建 | PASS | 三条命令均退出码 0；两端产物包含新类和样式。 |
| 真实 H5 页面视觉 | NOT_RUN | 本轮未使用真实列表数据打开页面。 |
| 微信开发者工具与真机视觉 | NOT_RUN | 已构建，未导入工具或真机检查。 |

## Rollback

撤销本轮列表行状态类及对应样式，不触及其他物品功能。
