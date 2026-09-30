package com.hahaen.ledger.todo.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.todo.entity.TodoDeliveryAttempt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import org.apache.ibatis.annotations.Param;
@Mapper public interface TodoDeliveryAttemptMapper extends BaseMapper<TodoDeliveryAttempt> {
    @Select("SELECT a.* FROM ha_todo_delivery_attempt a JOIN ha_todo_occurrence o ON o.id=a.occurrence_id WHERE a.occurrence_id=#{occurrenceId} AND o.user_id=#{userId} AND o.deleted=0 AND a.deleted=0 ORDER BY a.attempted_at DESC,a.id DESC LIMIT 100")
    List<TodoDeliveryAttempt> listOwned(@Param("userId") long userId,@Param("occurrenceId") long occurrenceId);
    @Update("UPDATE ha_todo_delivery_attempt SET result=#{result},updated_at=CURRENT_TIMESTAMP(3) WHERE id=#{id} AND result='SENDING'")
    int finish(@Param("id") long id,@Param("result") String result);
}
