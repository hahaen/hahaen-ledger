package com.hahaen.ledger.item.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.item.entity.PersonalItem;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PersonalItemMapper extends BaseMapper<PersonalItem> {
    @Select("SELECT id FROM app_user WHERE id = #{userId} AND deleted = 0 AND status = 'ACTIVE' FOR UPDATE")
    Long lockUser(@Param("userId") long userId);

    @Select("SELECT i.* FROM personal_item i JOIN app_user u ON u.id = i.user_id AND u.deleted = 0 AND u.status = 'ACTIVE' WHERE i.user_id = #{userId} AND i.deleted = 0 ORDER BY i.purchased_on DESC, i.id DESC")
    List<PersonalItem> listOwned(@Param("userId") long userId);

    @Select("SELECT * FROM personal_item WHERE user_id = #{userId} AND create_key = #{key}")
    PersonalItem byCreateKey(@Param("userId") long userId, @Param("key") String key);

    @Select("SELECT i.* FROM personal_item i JOIN app_user u ON u.id = i.user_id AND u.deleted = 0 AND u.status = 'ACTIVE' WHERE i.user_id = #{userId} AND i.id = #{id} AND i.deleted = 0")
    PersonalItem owned(@Param("userId") long userId, @Param("id") long id);

    // 调用前已锁用户行；包含删除记录仅用于验证删除请求的幂等重试。
    @Select("SELECT * FROM personal_item WHERE user_id = #{userId} AND id = #{id} FOR UPDATE")
    PersonalItem ownedForUpdate(@Param("userId") long userId, @Param("id") long id);

    @Update("""
        UPDATE personal_item SET deleted = 1, deleted_at = #{deletedAt}, deleted_by = #{deletedBy},
            deleted_name = #{deletedName}, updated_at = #{deletedAt}, updated_by = #{deletedBy},
            update_name = #{deletedName}, delete_key = #{deleteKey}
        WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
        """)
    int softDelete(PersonalItem item);
}
