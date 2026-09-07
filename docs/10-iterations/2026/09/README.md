# 2026 年 09 月迭代索引

本月档案集中记录业务域重置、数据库基线、用户、文件与资产账户表设计、H5 认证/MinIO 接入，以及 H5 页面回归和运行环境整改。

首页、日历、资产三模块的业务实现、测试和验证证据见 [`home-calendar-assets/`](home-calendar-assets/)。该迭代明确不修改数据库结构和 Flyway。`docs-comprehensive-audit/` 记录 2026-09-08 对全部 `docs/` 的真实性审计、规范修正和剩余风险。

## 阅读建议

先看 `business-domain-reset` 了解工程起点，再按数据依赖阅读 `app-user-schema`、`file-storage-schema`、`asset-account-schema`、`transaction-detail-refund-schema`，随后阅读 `h5-auth-minio` 和各个 H5 修复迭代。`third-round`、`third-round-governance` 和 `docs-comprehensive-audit` 属于审计/治理记录，结论应与 `docs/09-audit/` 一起核对。

## 当前目录约定

完整功能迭代应包含固定的 01–10 文件；只有 README 的目录表示目前只保留了索引说明或历史明细尚未恢复，不能据此推断功能已经完成。
