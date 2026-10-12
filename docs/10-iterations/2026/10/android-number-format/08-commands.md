# 实际命令与结果

2026-10-12，在 app 目录执行：

- 修复前 `node --test tests/android-number-format.test.mjs`：4 项失败。忽略 locale 参数复现 `235.7142857143`；Intl 不可用抛错；共享组件复现长小数；正常环境暴露舍入后负零。
- 修复后同一命令：4/4 PASS。24 组金额覆盖正常、忽略参数和 Intl 不可用运行时，另运行实际 MoneyDisplay 与 ItemCostChart 脚本。
- `node --test tests/*.test.mjs`：173/173 PASS，0 skipped。
- `pnpm run typecheck`：退出码 0，PASS。
- `pnpm run build:h5`、`pnpm run build:mp-weixin`：两端 Build complete，退出码 0；已有 Sass legacy-js-api 弃用提示，无构建错误。
- `node tests/android-number-format-build.cjs`：忽略参数、Intl 不可用两种环境均 PASS；执行真实微信生产包 MoneyDisplay/ItemCostChart 的 setup 与渲染函数，断言金额 `235.71`、总日资产 `497.79`、千分位 `27,695` 及五个刻度 `0 / 7.39 / 69.36 / 589.23 / 4,950`。
- 全局扫描 app/src：唯一 locale 格式化依赖为原 formatYuan，修复后无 toLocaleString/toLocaleDateString/toLocaleTimeString/Intl 调用；图表万/亿缩写继续使用 toFixed(1)。

本探针使用模拟运行时与渲染辅助函数，不启动微信、不获取真实账号或写业务数据。同步后的复验与 Git 结果见后续追加。

## 同步后复验

`git pull --ff-only` 返回 Already up to date；任务 stash 恢复后 19 个文件逐字节核对 PASS，无冲突。重新执行全量前端测试（173项）、typecheck、H5/微信构建及生产包渲染探针，均退出码 0。文档相对链接与 git diff --check 检查 PASS。提交前仅暂存本轮 19 个文件并审查差异，推送到 origin/main；实际提交哈希由 Git 交付结果报告。
