package com.hahaen.ledger.todo;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hahaen.ledger.todo.entity.TodoOccurrence;
import com.hahaen.ledger.todo.entity.TodoRule;
import com.hahaen.ledger.todo.mapper.TodoOccurrenceMapper;
import com.hahaen.ledger.todo.mapper.TodoRuleMapper;
import com.hahaen.ledger.todo.service.TodoMaterializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/** 显式 DEV 验收，仅用合成账号；不触发外部通知。 */
@SpringBootTest(properties={"logging.file.path=target/todo-dev-logs"})
@ActiveProfiles("dev")
@EnabledIfSystemProperty(named="todo.dev.verify", matches="true")
class TodoDevIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired TodoRuleMapper rules;
    @Autowired TodoOccurrenceMapper occurrences;
    @Autowired TodoMaterializer materializer;

    @Test void migrationSchemaAndMaterialization() {
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM haji_flyway_history WHERE version='10' AND success=1",Integer.class));
        for (String table : new String[]{"ha_todo_rule","ha_todo_occurrence","ha_todo_delivery","ha_todo_delivery_attempt","ha_todo_request"}) {
            assertEquals("utf8mb4_general_ci",jdbc.queryForObject("SELECT TABLE_COLLATION FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?",String.class,table));
            assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_COMMENT=''",Integer.class,table));
        }
        checkColumns("ha_todo_rule",TodoRule.class);
        checkColumns("ha_todo_occurrence",TodoOccurrence.class);
        long userId=IdWorker.getId();
        jdbc.update("INSERT INTO app_user(id,login_account,password_hash,nickname,status) VALUES(?,?,?,?,?)",userId,"todoqa"+userId,"synthetic-unusable-hash","待办验收","ACTIVE");
        try {
            TodoRule rule=new TodoRule();
            rule.setUserId(userId); rule.setCreatedBy(0L); rule.setCreatedName("系统"); rule.setTitle("合成待办"); rule.setRecurrence("DAILY"); rule.setMonthInterval(1);
            LocalDateTime anchor=LocalDateTime.of(2026,10,1,9,0);
            rule.setAnchorAt(anchor); rule.setNextDueAt(anchor.plusDays(1)); rule.setRemind(0); rule.setActive(1);
            assertEquals(1,rules.insert(rule));
            TodoOccurrence first=new TodoOccurrence();
            first.setRuleId(rule.getId());first.setUserId(userId);first.setDueAt(anchor);first.setAnchorAt(anchor);
            first.setCreatedBy(0L);first.setCreatedName("系统");first.setTitle("合成待办");first.setRecurrence("DAILY");first.setMonthInterval(1);first.setRemind(0);first.setStatus("PENDING");
            assertEquals(1,occurrences.insert(first));
            assertEquals(1,occurrences.count(userId,"PENDING"));
            materializer.generate(rule.getId(),anchor.plusDays(2),false);
            assertEquals(3,occurrences.count(userId,"PENDING"));
            assertEquals(anchor.plusDays(3),rules.selectById(rule.getId()).getNextDueAt());
        } finally {
            jdbc.update("DELETE FROM ha_todo_occurrence WHERE user_id=?",userId);
            jdbc.update("DELETE FROM ha_todo_rule WHERE user_id=?",userId);
            jdbc.update("DELETE FROM app_user WHERE id=?",userId);
        }
    }
    private void checkColumns(String table,Class<?> entity) {
        Set<String> actual=jdbc.queryForList("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?",String.class,table).stream().collect(Collectors.toSet());
        Set<String> expected=new java.util.HashSet<>();
        for(Class<?> type=entity;type!=Object.class;type=type.getSuperclass())
            for(var field:type.getDeclaredFields()) if(!java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                expected.add(field.getName().replaceAll("([A-Z])","_$1").toLowerCase(Locale.ROOT));
        assertEquals(expected,actual);
    }
}
