package com.hahaen.ledger.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.todo.entity.TodoDelivery;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TodoDeliveryMapper extends BaseMapper<TodoDelivery> {
    @Insert("INSERT IGNORE INTO ha_todo_delivery(id,occurrence_id,channel,status,attempts,next_attempt_at,created_by,created_name,deleted) VALUES(#{id},#{occurrenceId},#{channel},'PENDING',0,#{now},#{systemUserId},'系统',0)")
    int ensure(@Param("id") long id,@Param("occurrenceId") long occurrenceId,@Param("channel") String channel,@Param("now") LocalDateTime now,@Param("systemUserId") long systemUserId);
    @Select("SELECT * FROM ha_todo_delivery WHERE deleted=0 AND ((status='PENDING' AND next_attempt_at<=#{now}) OR (status='SENDING' AND locked_until<=#{now})) ORDER BY next_attempt_at,id LIMIT 100")
    List<TodoDelivery> ready(@Param("now") LocalDateTime now);
    @Update("UPDATE ha_todo_delivery SET status='SENDING',attempts=attempts+1,locked_until=#{until},updated_at=CURRENT_TIMESTAMP(3) WHERE id=#{id} AND ((status='PENDING' AND next_attempt_at<=#{now}) OR (status='SENDING' AND locked_until<=#{now})) AND deleted=0")
    int claim(@Param("id") long id,@Param("now") LocalDateTime now,@Param("until") LocalDateTime until);
    @Update("UPDATE ha_todo_delivery SET status='SENT',sent_at=CURRENT_TIMESTAMP(3),locked_until=NULL,updated_at=CURRENT_TIMESTAMP(3) WHERE id=#{id} AND status='SENDING'")
    int sent(@Param("id") long id);
    @Update("UPDATE ha_todo_delivery SET status=#{status},locked_until=NULL,updated_at=CURRENT_TIMESTAMP(3) WHERE id=#{id} AND status='SENDING'")
    int finish(@Param("id") long id,@Param("status") String status);
    @Update("UPDATE ha_todo_delivery SET status='PENDING',next_attempt_at=#{nextAttempt},locked_until=NULL,updated_at=CURRENT_TIMESTAMP(3) WHERE id=#{id} AND status='SENDING'")
    int retry(@Param("id") long id,@Param("nextAttempt") LocalDateTime nextAttempt);
}
