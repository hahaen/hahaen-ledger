package com.hahaen.ledger.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.account.entity.AssetAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssetAccountMapper extends BaseMapper<AssetAccount> {
    @Select("""
            SELECT * FROM asset_account
             WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
             FOR UPDATE
            """)
    AssetAccount selectOwnedForUpdate(@Param("id") long id, @Param("userId") long userId);

    @Select("""
            SELECT * FROM asset_account
             WHERE user_id = #{userId} AND deleted = 0
             ORDER BY account_type ASC, account_name ASC, id ASC
            """)
    List<AssetAccount> selectActiveByUser(@Param("userId") long userId);
}
