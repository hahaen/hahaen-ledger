package com.hahaen.ledger.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.todo.entity.TodoRule;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TodoRuleMapper extends BaseMapper<TodoRule> {
    @Select("SELECT id FROM app_user WHERE id=#{userId} AND deleted=0 AND status='ACTIVE' FOR UPDATE")
    Long lockUser(@Param("userId") long userId);
    @Select("SELECT id FROM app_user WHERE id=#{userId} AND deleted=0 AND status='ACTIVE'")
    Long activeUser(@Param("userId") long userId);
    @Select("SELECT * FROM ha_todo_rule WHERE id=#{id} AND user_id=#{userId} AND deleted=0 FOR UPDATE")
    TodoRule ownedForUpdate(@Param("userId") long userId, @Param("id") long id);
    @Select("SELECT * FROM ha_todo_rule WHERE id=#{id} AND deleted=0 AND active=1 FOR UPDATE")
    TodoRule dueForUpdate(@Param("id") long id);
    @Select("SELECT id FROM ha_todo_rule WHERE deleted=0 AND active=1 AND next_due_at <= #{horizon} ORDER BY next_due_at,id LIMIT 100")
    List<Long> dueRuleIds(@Param("horizon") LocalDateTime horizon);
    @Update("UPDATE ha_todo_rule SET next_due_at=#{nextDueAt},updated_at=CURRENT_TIMESTAMP(3) WHERE id=#{id} AND deleted=0 AND active=1")
    int advance(@Param("id") long id, @Param("nextDueAt") LocalDateTime nextDueAt);
    @Update("""
      UPDATE ha_todo_rule SET title=#{title},note=#{note},recurrence=#{recurrence},month_interval=#{monthInterval},
      repeat_mode=#{repeatMode},repeat_unit=#{repeatUnit},repeat_interval=#{repeatInterval},
      week_days=#{weekDays},month_days=#{monthDays},last_day=#{lastDay},year_days=#{yearDays},fixed_dates=#{fixedDates},
      anchor_at=#{anchorAt},next_due_at=#{nextDueAt},remind=#{remind},active=1,
      updated_at=CURRENT_TIMESTAMP(3),updated_by=#{userId},update_name=#{updateName}
      WHERE id=#{id} AND user_id=#{userId} AND deleted=0
      """)
    int edit(TodoRule rule);
    @Update("UPDATE ha_todo_rule SET active=0,next_due_at=NULL,updated_at=CURRENT_TIMESTAMP(3),updated_by=#{userId},update_name=#{userName} WHERE id=#{id} AND user_id=#{userId} AND deleted=0")
    int end(@Param("id") long id,@Param("userId") long userId,@Param("userName") String userName);
    @Select("SELECT request_hash FROM ha_todo_request WHERE user_id=#{userId} AND idempotency_key=#{key} AND deleted=0")
    String requestHash(@Param("userId") long userId,@Param("key") String key);
    @Select("SELECT result_id FROM ha_todo_request WHERE user_id=#{userId} AND idempotency_key=#{key} AND deleted=0")
    Long requestResult(@Param("userId") long userId,@Param("key") String key);
    @Insert("INSERT INTO ha_todo_request(user_id,idempotency_key,request_hash,result_id,deleted) VALUES(#{userId},#{key},#{hash},#{resultId},0)")
    int saveRequest(@Param("userId") long userId,@Param("key") String key,@Param("hash") String hash,@Param("resultId") Long resultId);
}
