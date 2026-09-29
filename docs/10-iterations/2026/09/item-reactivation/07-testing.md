# 测试

- `ItemServiceTest` 新增重新服役字段清空、天数/净成本/日均成本重算、同键重试、旧键重放、跨用户/删除/在役拒绝用例；定向运行 14 项 PASS。
- `ItemDevIntegrationTest` 使用隔离合成用户经真实登录、HTTP、MySQL 测试跨用户拒绝、退役→重新服役→再次退役、同键重试与状态变化冲突；直接查询主表确认两个字段为 NULL，并核对 V8 关系表列、注释和公共字段默认值；测试 PASS，合成用户与物品在 finally 中清理。证据见 `evidence/dev-api.json`。
- 前端 `items.test.mjs` 与 `date-picker-scroll.test.mjs` 共 9 项 PASS；`vue-tsc`、H5 和微信小程序生产构建 PASS。
- 真实 H5 点击确认与微信开发者工具/真机操作 NOT_RUN；双端构建不能代替视觉验收。
- 后端全量 `mvn test` 为 FAIL：68 项中 1 项 `LoggingProfileConfigTest` 在 macOS 实际目录下仍断言 Windows 路径 `D:/github/log/haji`；不属于物品逻辑，已保留失败记录。
