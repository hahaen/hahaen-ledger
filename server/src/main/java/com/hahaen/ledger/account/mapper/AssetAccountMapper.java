package com.hahaen.ledger.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.account.entity.AssetAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AssetAccountMapper extends BaseMapper<AssetAccount> {
    /** 普通 updateById 会排除 @TableLogic 字段，删除必须显式更新并校验归属。 */
    @Update("""
            UPDATE asset_account
               SET deleted = 1,
                   deleted_at = #{account.deletedAt},
                   deleted_by = #{account.deletedBy},
                   deleted_name = #{account.deletedName},
                   updated_at = #{account.deletedAt},
                   updated_by = #{account.deletedBy},
                   update_name = #{account.deletedName}
             WHERE id = #{account.id} AND user_id = #{account.userId} AND deleted = 0
            """)
    int softDeleteById(@Param("account") AssetAccount account);

    @Select("""
            SELECT * FROM asset_account
             WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
             FOR UPDATE
            """)
    AssetAccount selectOwnedForUpdate(@Param("id") long id, @Param("userId") long userId);

    @Select("""
            SELECT sort_order FROM asset_account
             WHERE user_id = #{userId} AND account_type = #{accountType} AND deleted = 0
             ORDER BY sort_order DESC, id DESC
             LIMIT 1 FOR UPDATE
            """)
    Integer selectMaxSortOrderForUpdate(@Param("userId") long userId, @Param("accountType") String accountType);

    @Select("""
            SELECT * FROM asset_account
             WHERE user_id = #{userId} AND deleted = 0
             ORDER BY account_type ASC, sort_order ASC, id ASC
            """)
    List<AssetAccount> selectActiveByUser(@Param("userId") long userId);
}
