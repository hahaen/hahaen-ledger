package com.hahaen.ledger.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.user.entity.UserNotificationConfig;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserNotificationConfigMapper extends BaseMapper<UserNotificationConfig> {
    @Select("SELECT id FROM app_user WHERE id = #{userId} AND status = 'ACTIVE' AND deleted = 0")
    Long activeUser(@Param("userId") long userId);

    @Select("SELECT id FROM app_user WHERE id = #{userId} AND status = 'ACTIVE' AND deleted = 0 FOR UPDATE")
    Long lockActiveUser(@Param("userId") long userId);

    @Select("SELECT * FROM user_notification_config WHERE user_id = #{userId} AND notification_type = #{type} FOR UPDATE")
    UserNotificationConfig findIncludingDeleted(@Param("userId") long userId, @Param("type") String type);

    @Update("""
        UPDATE user_notification_config SET notification_key = #{encrypted}, deleted = 0,
          deleted_at = NULL, deleted_by = NULL, deleted_name = NULL,
          updated_at = CURRENT_TIMESTAMP(3), updated_by = #{userId}, update_name = #{userName}
        WHERE id = #{id} AND user_id = #{userId}
        """)
    int restoreOrUpdate(@Param("id") long id, @Param("userId") long userId,
                        @Param("userName") String userName, @Param("encrypted") String encrypted);

    @Update("""
        UPDATE user_notification_config SET deleted = 1, deleted_at = CURRENT_TIMESTAMP(3),
          deleted_by = #{userId}, deleted_name = #{userName}, updated_at = CURRENT_TIMESTAMP(3),
          updated_by = #{userId}, update_name = #{userName}
        WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
        """)
    int softDelete(@Param("id") long id, @Param("userId") long userId, @Param("userName") String userName);

    @Select("SELECT request_hash FROM user_notification_config_request WHERE user_id = #{userId} AND idempotency_key = #{key} AND deleted = 0")
    String requestHash(@Param("userId") long userId, @Param("key") String key);

    @Insert("INSERT INTO user_notification_config_request (user_id, idempotency_key, request_hash, deleted) VALUES (#{userId}, #{key}, #{hash}, 0)")
    int insertRequest(@Param("userId") long userId, @Param("key") String key, @Param("hash") String hash);
}
