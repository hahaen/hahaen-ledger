# 第二轮工程审计报告（历史）

本文件从旧的 `docs/audit-report.md` 迁移而来，保留第二轮历史结果，不代表当前数据库初始化顺序。当前唯一数据库基线为 `V1__init_schema.sql`，以 `third-round-audit.md` 和 `flyway-v1-baseline` iteration 为准。

真实微信登录在第二轮仍因平台 AppID、合法域名和权限条件缺失而 BLOCKED；第三轮继续沿用这一外部条件限制，不能用 dev code 代替生产验收。
