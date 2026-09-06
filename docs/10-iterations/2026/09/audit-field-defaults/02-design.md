# 设计

新增 V4，不改历史 V1-V3。主表及关联表使用 DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)。取消无登录身份时以记录 ID 或系统 ID 兜底创建人的行为；有身份时继续自动填充。正常应用插入填充 createdAt 和 deleted，数据库默认值支持省略列的 SQL。
