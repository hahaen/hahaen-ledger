# 数据库

新增 V6__create_personal_item_table.sql，不改 V1–V5。personal_item 为独立主表，继承10个正式公共字段；user_id 外键、人民币分、DATE、ACTIVE/RETIRED、购入/退役/删除幂等键。用户创建键唯一且删除后保留，防止超时重试重复插入。写入先锁定有效 app_user，再查询归属数据，串行保护幂等。新建表无历史收缩风险。数据库实际迁移、information_schema 验证待运行。
