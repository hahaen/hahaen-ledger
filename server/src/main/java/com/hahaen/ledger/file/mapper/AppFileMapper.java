package com.hahaen.ledger.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.file.entity.AppFile;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppFileMapper extends BaseMapper<AppFile> {
    @Update("""
            UPDATE app_file
               SET status = #{file.status}, failure_code = #{file.failureCode},
                   deleted_at = #{file.deletedAt}, deleted_by = #{file.deletedBy},
                   deleted_name = #{file.deletedName}, updated_at = CURRENT_TIMESTAMP(3),
                   updated_by = #{file.deletedBy}, update_name = #{file.deletedName}, deleted = 1
             WHERE id = #{file.id} AND user_id = #{userId} AND deleted = 0
            """)
    int markDeleted(@Param("file") AppFile file, @Param("userId") long userId);
}
