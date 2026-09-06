# 10｜迭代档案

本目录按 `YYYY/MM/feature-key/` 保存历史变更。每个重大功能、Bug、数据库、权限、基础设施或设计整改，都应有一个独立 feature-key，避免把多次变更混成无法审计的长文档。

## 固定文件

| 文件 | 说明 |
| --- | --- |
| `README.md` | 本次迭代摘要、边界、状态和文件导航。 |
| `01-requirement.md` | 需求来源、目标、范围、非目标和验收条件。 |
| `02-design.md` | 方案、关键决策、替代方案和影响面。 |
| `03-database.md` | Migration、字段、索引、约束和数据风险。 |
| `04-api.md` | 接口、DTO/VO、错误、权限和幂等变化。 |
| `05-backend.md` | Entity、Mapper、Service、Controller、事务和日志实现。 |
| `06-frontend.md` | 页面、状态、API 调用、平台差异和交互变化。 |
| `07-testing.md` | 测试范围、用例、依赖、预期和实际结果。 |
| `08-commands.md` | 真实执行的命令及结果，不放猜测命令。 |
| `09-verification.md` | 分项状态和证据，区分 PASS/PARTIAL/FAIL/BLOCKED/NOT_RUN。 |
| `10-rollback.md` | 未执行和已执行两种情况下的安全回滚策略。 |

## 编写边界

历史档案可以记录当时的缺口，但不能将后续实现倒灌成当时已完成；共享环境执行过的 Migration 不修改、不删除，结构变化通过新的 Migration 记录。
