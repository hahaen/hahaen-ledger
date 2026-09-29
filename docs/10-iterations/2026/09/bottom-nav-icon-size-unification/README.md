# 底部导航图标尺寸统一

日期：2026-09-28

## Requirement

首页、日历、资产、物品、我的五个底部导航图标均与当前“物品”图标视觉大小一致。

## Design

保留五个图标的原有造型与 25px 图标容器，移除字符图标的额外缩放。首次使用统一 22px 字符字号后，浏览器反馈首页、日历仍小，物品仍大；按实际字形留白细调为首页 24px、日历 27px、资产和我的 23px，并将物品描边框从 16×17px 收到 14×15px。再次反馈首页、日历偏下，按 H5 截图测量将两者上移 2px，使五项可见轮廓的纵向中心都约为截图裁剪区的 30px。以可见轮廓而非相同 CSS 字号为对齐依据。

## Database

NOT_RUN：不涉及数据库。

## API

NOT_RUN：不涉及 API。

## Backend

NOT_RUN：不涉及后端。

## Frontend

仅调整 `app/src/prototype.scss` 的底栏字符图标字号、缩放规则、首页和日历字形位置及物品描边框尺寸，不改变导航布局、文字和选中背景。

## Testing

PASS：H5 浏览器底栏画面已核对并保存局部截图；类型检查与双端构建均成功。微信开发者工具及真机视觉为 NOT_RUN。

## Commands

- `pnpm run typecheck`：PASS，退出码 0。
- `pnpm run build:h5`：PASS，退出码 0。
- `pnpm run build:mp-weixin`：PASS，退出码 0。

## Verification

- PASS：本机 H5 底栏浏览器画面核对五个图标视觉大小和位置，截图保存在 `app/tests/evidence/bottom-nav-icons-20260928.jpg`。截图像素检查显示上移前首页、日历轮廓中心分别为 31.5px 和 32px，上移后分别为 29.5px 和 30px，其余三项约为 30px。
- NOT_RUN：微信开发者工具与真机实际画面。

## Rollback

恢复字符图标 19px 字号、原缩放规则、物品描边框 16×17px 尺寸及相关规范记录。
