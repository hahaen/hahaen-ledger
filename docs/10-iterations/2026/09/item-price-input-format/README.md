# 物品价格输入与回显格式

## Requirement

物品新增、编辑时，整数购买价格不显示 `.00`；有角分的价格保留两位小数。新增输入框的空值提示显示 `0`。

## Design

编辑回显复用现有 `inputYuan`，避免千分位分隔符进入输入框；新增和编辑的占位文字统一为 `0`。

## Database / API / Backend

无变更。请求仍以整数分传输，物品资料和成本计算口径不变。

## Frontend

只调整 `ItemEditor` 与 `ItemProfileEditor` 的购买价格输入框及编辑初始化。

## Testing

金额回归明确断言输入回显为 `0`、`1`、`1.20`、`1000`，并覆盖物品金额解析。

## Commands

在 `app/` 执行：

- `node --test tests/entry.test.mjs tests/items.test.mjs`：9/9 通过。
- `pnpm run typecheck`：通过。
- `pnpm run build:h5`：通过。
- `pnpm run build:mp-weixin`：通过。

## Verification

PASS：上述定向回归、TypeScript 检查、H5 与微信小程序构建。PARTIAL：代码与构建确认新增占位符和编辑回显调用，但未在真实登录页面操作新增/编辑。NOT_RUN：微信开发者工具和真机视觉验收。

## Rollback

如需回退，仅恢复本次两个物品编辑组件的价格输入格式；不涉及数据迁移。
