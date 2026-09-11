-- app_user 只保存稳定的对象 Key；不保存会变更的 MinIO 域名、Bucket URL 或签名参数。
-- 已就绪且未删除的旧头像在删除旧外键前回填，异常/未完成文件不作为当前头像迁移。

ALTER TABLE app_user
  ADD COLUMN avatar_file_url VARCHAR(512) NULL COMMENT '头像对象Key，不含可变MinIO访问前缀，未设置时为空' AFTER nickname;

UPDATE app_user AS user_record
JOIN app_file AS file_record ON file_record.id = user_record.avatar_file_id
  SET user_record.avatar_file_url = file_record.object_key
WHERE file_record.business_type = 'AVATAR'
  AND file_record.status = 'READY'
  AND file_record.deleted = 0;

ALTER TABLE app_user
  DROP FOREIGN KEY fk_app_user_avatar_file,
  DROP COLUMN avatar_file_id;

ALTER TABLE app_file
  ADD KEY idx_app_file_user_avatar_hash (user_id, business_type, file_hash, status, deleted);
