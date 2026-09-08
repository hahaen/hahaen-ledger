# 验证

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 后端单测 | `server/mvn test`，19 tests 全部通过 | PASS |
| 前端类型检查 | `app/pnpm run typecheck` exit 0 | PASS |
| H5 构建 | `app/pnpm run build:h5` 完成 | PASS |
| 微信小程序构建 | `app/pnpm run build:mp-weixin` 完成 | PASS（仅构建） |
| Flyway/MySQL 实际执行 | 当前无可用 3306；未核对 `flyway_schema_history`/`information_schema` | BLOCKED |
| 有效会话下长按换序 | 未取得真实会话 | NOT_RUN |
| 微信开发者工具人工验收 | 未执行 | NOT_RUN |

## 交互微调复核

| 范围 | 证据 | 状态 |
| --- | --- | --- |
| 顶部换序提示条移除 | `assets.vue` 不再渲染 `.account-reorder-tip` | PASS |
| 账户类型隔离显示“交换” | `reorderingKind` 仅匹配当前长按的 `FUND`/`CREDIT` 列表 | PASS |
| “交换”文字可读性 | `.reorder-label` 使用 11px、较深颜色和 600 字重 | PASS |
| 换序点击范围 | 账户行改为普通 `view` 容器，右侧独立“交换”控件使用 `@click.stop.prevent` 单独触发 | PASS |
| 不计入净资产角标 | 资金账户和信贷账户仅在 `includedInNetAsset=false` 时显示“不计入” | PASS |
| H5/微信小程序构建 | 本次微调后两类构建均完成 | PASS（仅构建） |
