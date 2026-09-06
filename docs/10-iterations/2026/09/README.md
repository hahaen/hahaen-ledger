# 2026 年 09 月迭代索引

本月档案集中记录业务域重置、数据库基线、用户与文件表设计、H5 认证/MinIO 接入，以及 H5 页面回归和运行环境整改。

## 阅读建议

先看 `business-domain-reset` 了解工程起点，再按数据依赖阅读 `app-user-schema`、`file-storage-schema`，随后阅读 `h5-auth-minio` 和各个 H5 修复迭代。`third-round` 与 `third-round-governance` 属于阶段审计和治理记录，结论应与 `docs/09-audit/` 一起核对。

## 当前目录约定

完整功能迭代应包含固定的 01–10 文件；只有 README 的目录表示目前只保留了索引说明或历史明细尚未恢复，不能据此推断功能已经完成。
