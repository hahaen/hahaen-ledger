package com.hahaen.ledger.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.todo.entity.TodoOccurrence;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TodoOccurrenceMapper extends BaseMapper<TodoOccurrence> {
    @Select("SELECT * FROM ha_todo_occurrence WHERE id=#{id} AND user_id=#{userId} AND deleted=0 AND status='PENDING'")
    TodoOccurrence pendingOwned(@Param("userId") long userId,@Param("id") long id);
    @Select("SELECT * FROM ha_todo_occurrence WHERE id=#{id} AND user_id=#{userId} AND deleted=0 FOR UPDATE")
    TodoOccurrence ownedForUpdate(@Param("userId") long userId,@Param("id") long id);
    @Select("""
      SELECT o.* FROM ha_todo_occurrence o JOIN ha_todo_rule r ON r.id=o.rule_id
      WHERE o.user_id=#{userId} AND o.deleted=0 AND o.status=#{status}
      ORDER BY CASE WHEN #{status}='PENDING' THEN o.due_at END ASC, CASE WHEN #{status}='COMPLETED' THEN o.completed_at END DESC,o.id DESC LIMIT #{limit} OFFSET #{offset}
      """)
    List<TodoOccurrence> list(@Param("userId") long userId,@Param("status") String status,@Param("limit") int limit,@Param("offset") int offset);
    @Select("SELECT status,COUNT(*) FROM ha_todo_occurrence WHERE user_id=#{userId} AND deleted=0 GROUP BY status")
    @MapKey("status") java.util.Map<String,java.util.Map<String,Object>> counts(@Param("userId") long userId);
    @Select("SELECT COUNT(*) FROM ha_todo_occurrence WHERE user_id=#{userId} AND deleted=0 AND status=#{status}")
    long count(@Param("userId") long userId,@Param("status") String status);
    @Select("SELECT MAX(due_at) FROM ha_todo_occurrence WHERE rule_id=#{ruleId} AND user_id=#{userId} AND status='COMPLETED'")
    LocalDateTime lastCompletedDue(@Param("ruleId") long ruleId,@Param("userId") long userId);
    @Select("SELECT * FROM ha_todo_occurrence WHERE id=#{id} AND deleted=0 AND status='PENDING'")
    TodoOccurrence pending(@Param("id") long id);
    @Select("""
      SELECT o.* FROM ha_todo_occurrence o JOIN ha_todo_rule r ON r.id=o.rule_id
      JOIN app_user u ON u.id=o.user_id AND u.deleted=0 AND u.status='ACTIVE'
      WHERE o.deleted=0 AND o.status='PENDING' AND r.deleted=0 AND o.remind=1
        AND o.due_at<=#{now} AND o.due_at>#{oldest} AND o.id>#{afterId}
      ORDER BY o.id LIMIT 200
      """)
    List<TodoOccurrence> dueForReminder(@Param("now") LocalDateTime now,@Param("oldest") LocalDateTime oldest,@Param("afterId") long afterId);
    @Update("UPDATE ha_todo_occurrence SET status='COMPLETED',completed_at=CURRENT_TIMESTAMP(3),updated_at=CURRENT_TIMESTAMP(3),updated_by=#{userId},update_name=#{userName} WHERE id=#{id} AND user_id=#{userId} AND status='PENDING' AND deleted=0")
    int complete(@Param("id") long id,@Param("userId") long userId,@Param("userName") String userName);
    @Update("""
      UPDATE ha_todo_occurrence SET deleted=1,deleted_at=CURRENT_TIMESTAMP(3),deleted_by=#{userId},deleted_name=#{userName},
      updated_at=CURRENT_TIMESTAMP(3),updated_by=#{userId},update_name=#{userName}
      WHERE rule_id=#{ruleId} AND user_id=#{userId} AND status='PENDING' AND deleted=0
      """)
    int deletePendingRule(@Param("ruleId") long ruleId,@Param("userId") long userId,@Param("userName") String userName);
    @Update("""
      UPDATE ha_todo_occurrence SET deleted=1,deleted_at=CURRENT_TIMESTAMP(3),deleted_by=#{userId},deleted_name=#{userName},
      updated_at=CURRENT_TIMESTAMP(3),updated_by=#{userId},update_name=#{userName}
      WHERE id=#{id} AND user_id=#{userId} AND deleted=0
      """)
    int deleteOne(@Param("id") long id,@Param("userId") long userId,@Param("userName") String userName);
}
