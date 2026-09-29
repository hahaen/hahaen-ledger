package com.hahaen.ledger.item.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.item.entity.PersonalItem;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PersonalItemMapper extends BaseMapper<PersonalItem> {
    @Select("SELECT id FROM app_user WHERE id = #{userId} AND deleted = 0 AND status = 'ACTIVE' FOR UPDATE")
    Long lockUser(@Param("userId") long userId);

    @Select("SELECT i.* FROM personal_item i JOIN app_user u ON u.id = i.user_id AND u.deleted = 0 AND u.status = 'ACTIVE' WHERE i.user_id = #{userId} AND i.deleted = 0 ORDER BY CASE WHEN i.status = 'ACTIVE' THEN 0 ELSE 1 END, i.purchased_on DESC, i.id DESC")
    List<PersonalItem> listOwned(@Param("userId") long userId);

    @Select("SELECT * FROM personal_item WHERE user_id = #{userId} AND create_key = #{key}")
    PersonalItem byCreateKey(@Param("userId") long userId, @Param("key") String key);

    @Select("SELECT i.* FROM personal_item i JOIN app_user u ON u.id = i.user_id AND u.deleted = 0 AND u.status = 'ACTIVE' WHERE i.user_id = #{userId} AND i.id = #{id} AND i.deleted = 0")
    PersonalItem owned(@Param("userId") long userId, @Param("id") long id);

    // 调用前已锁用户行；包含删除记录仅用于验证删除请求的幂等重试。
    @Select("SELECT * FROM personal_item WHERE user_id = #{userId} AND id = #{id} FOR UPDATE")
    PersonalItem ownedForUpdate(@Param("userId") long userId, @Param("id") long id);

    @Select("SELECT request_hash FROM personal_item_edit_request WHERE user_id = #{userId} AND idempotency_key = #{key} AND deleted = 0")
    String editRequestHash(@Param("userId") long userId, @Param("key") String key);

    @Insert("INSERT INTO personal_item_edit_request (user_id, idempotency_key, item_id, request_hash, deleted) VALUES (#{userId}, #{key}, #{itemId}, #{hash}, 0)")
    int insertEditRequest(@Param("userId") long userId, @Param("key") String key,
        @Param("itemId") long itemId, @Param("hash") String hash);

    @Select("SELECT item_id FROM personal_item_reactivate_request WHERE user_id = #{userId} AND idempotency_key = #{key} AND deleted = 0")
    Long reactivateRequestItemId(@Param("userId") long userId, @Param("key") String key);

    @Update("""
        UPDATE personal_item SET status = 'ACTIVE', retired_on = NULL, resale_cent = NULL,
            updated_at = CURRENT_TIMESTAMP(3), updated_by = #{userId}, update_name = #{userName}
        WHERE id = #{itemId} AND user_id = #{userId} AND deleted = 0 AND status = 'RETIRED'
        """)
    int reactivateOwned(@Param("userId") long userId, @Param("itemId") long itemId,
        @Param("userName") String userName);

    @Insert("INSERT INTO personal_item_reactivate_request (user_id, idempotency_key, item_id, deleted) VALUES (#{userId}, #{key}, #{itemId}, 0)")
    int insertReactivateRequest(@Param("userId") long userId, @Param("key") String key, @Param("itemId") long itemId);

    @Update("""
        UPDATE personal_item SET deleted = 1, deleted_at = #{deletedAt}, deleted_by = #{deletedBy},
            deleted_name = #{deletedName}, updated_at = #{deletedAt}, updated_by = #{deletedBy},
            update_name = #{deletedName}, delete_key = #{deleteKey}
        WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
        """)
    int softDelete(PersonalItem item);
}
