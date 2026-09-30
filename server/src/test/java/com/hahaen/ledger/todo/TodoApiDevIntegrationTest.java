package com.hahaen.ledger.todo;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"logging.file.path=target/todo-api-dev-logs"})
@ActiveProfiles("dev")
@EnabledIfSystemProperty(named="todo.dev.verify",matches="true")
class TodoApiDevIntegrationTest {
    @Autowired TestRestTemplate http;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @Test void createCompleteEditDeleteAndOwnerIsolation() throws Exception {
        long userId=IdWorker.getId(), otherId=IdWorker.getId();
        String account="todoqa"+userId, other="todoqa"+otherId, password=UUID.randomUUID().toString();
        jdbc.update("INSERT INTO app_user(id,login_account,password_hash,nickname,status) VALUES(?,?,?,?,?)",userId,account,encoder.encode(password),"待办API验收","ACTIVE");
        jdbc.update("INSERT INTO app_user(id,login_account,password_hash,nickname,status) VALUES(?,?,?,?,?)",otherId,other,encoder.encode(password),"待办API验收2","ACTIVE");
        try {
            String publicKey=data(call(HttpMethod.GET,"/api/app/auth/password-key",null,null)).path("publicKey").asText();
            String token=login(publicKey,account,password), otherToken=login(publicKey,other,password);
            String due=LocalDateTime.now(ZoneId.of("Asia/Shanghai")).plusHours(4).withSecond(0).withNano(0).toString();
            Map<String,Object> create=Map.of("title","浇水","note","阳台","recurrence","DAILY","monthInterval",1,"dueAt",due,"remind",false,"idempotencyKey","todo-create-12345");
            String ruleId=data(call(HttpMethod.POST,"/api/app/todos",create,token)).asText();
            assertEquals(ruleId,data(call(HttpMethod.POST,"/api/app/todos",create,token)).asText());
            JsonNode pending=data(call(HttpMethod.GET,"/api/app/todos?status=PENDING",null,token));
            assertEquals(1,pending.path("pendingCount").asInt());
            String id=pending.path("items").get(0).path("id").asText();
            assertEquals("浇水",data(call(HttpMethod.GET,"/api/app/todos/"+id,null,token)).path("title").asText());
            assertNotEquals(0,call(HttpMethod.GET,"/api/app/todos/"+id,null,otherToken).path("code").asInt());
            assertEquals(0,data(call(HttpMethod.GET,"/api/app/todos?status=PENDING",null,otherToken)).path("pendingCount").asInt());
            assertNotEquals(0,call(HttpMethod.GET,"/api/app/todos/"+id+"/attempts",null,otherToken).path("code").asInt());
            data(call(HttpMethod.POST,"/api/app/todos/"+id+"/complete",Map.of("idempotencyKey","todo-complete-12345"),token));
            assertNotEquals(0,call(HttpMethod.GET,"/api/app/todos/"+id,null,token).path("code").asInt());
            data(call(HttpMethod.POST,"/api/app/todos/"+id+"/complete",Map.of("idempotencyKey","todo-complete-12345"),token));
            JsonNode completed=data(call(HttpMethod.GET,"/api/app/todos?status=COMPLETED",null,token));
            assertEquals(1,completed.path("completedCount").asInt());
            assertEquals(1,completed.path("pendingCount").asInt());
            String nextId=data(call(HttpMethod.GET,"/api/app/todos?status=PENDING",null,token)).path("items").get(0).path("id").asText();
            data(call(HttpMethod.PUT,"/api/app/todos/"+nextId,Map.of("title","浇水改名","note","阳台","recurrence","DAILY","monthInterval",1,"dueAt",due,"remind",false,"idempotencyKey","todo-edit-same-anchor-12345"),token));
            JsonNode afterSameAnchor=data(call(HttpMethod.GET,"/api/app/todos?status=PENDING",null,token));
            assertEquals(1,afterSameAnchor.path("pendingCount").asInt());
            assertEquals(LocalDateTime.parse(due).plusDays(1),LocalDateTime.parse(afterSameAnchor.path("items").get(0).path("dueAt").asText()));
            nextId=afterSameAnchor.path("items").get(0).path("id").asText();
            String nextDue=afterSameAnchor.path("items").get(0).path("dueAt").asText();
            data(call(HttpMethod.PUT,"/api/app/todos/"+nextId,Map.of("title","仅改标题","note","阳台","recurrence","DAILY","monthInterval",1,"dueAt",nextDue,"remind",false,"idempotencyKey","todo-edit-next-due-12345"),token));
            JsonNode afterTitleEdit=data(call(HttpMethod.GET,"/api/app/todos?status=PENDING",null,token)).path("items").get(0);
            assertEquals(LocalDateTime.parse(due),LocalDateTime.parse(afterTitleEdit.path("anchorAt").asText()));
            assertEquals(LocalDateTime.parse(nextDue),LocalDateTime.parse(afterTitleEdit.path("dueAt").asText()));
            nextId=afterTitleEdit.path("id").asText();
            String newDue=LocalDateTime.now(ZoneId.of("Asia/Shanghai")).plusDays(2).withSecond(0).withNano(0).toString();
            data(call(HttpMethod.PUT,"/api/app/todos/"+nextId,Map.of("title","换水","note","","recurrence","ONCE","monthInterval",1,"dueAt",newDue,"remind",false,"idempotencyKey","todo-edit-12345"),token));
            assertEquals("浇水",data(call(HttpMethod.GET,"/api/app/todos?status=COMPLETED",null,token)).path("items").get(0).path("title").asText());
            long deliveryId=IdWorker.getId(),attemptId=IdWorker.getId();
            jdbc.update("INSERT INTO ha_todo_delivery(id,occurrence_id,channel,status,attempts,next_attempt_at) VALUES(?,?,'BARK','SENT',1,CURRENT_TIMESTAMP(3))",deliveryId,Long.valueOf(id));
            jdbc.update("INSERT INTO ha_todo_delivery_attempt(id,delivery_id,occurrence_id,channel,message_title,message_body,result,attempted_at) VALUES(?,?,?,'BARK','待办清单提醒','浇水','ACCEPTED',CURRENT_TIMESTAMP(3))",attemptId,deliveryId,Long.valueOf(id));
            JsonNode history=data(call(HttpMethod.GET,"/api/app/todos/"+id+"/attempts",null,token));
            assertEquals("BARK",history.get(0).path("channel").asText());
            assertEquals("浇水",history.get(0).path("messageBody").asText());
            String editedId=data(call(HttpMethod.GET,"/api/app/todos?status=PENDING",null,token)).path("items").get(0).path("id").asText();
            assertEquals("换水",data(call(HttpMethod.GET,"/api/app/todos?status=PENDING",null,token)).path("items").get(0).path("title").asText());
            assertNotEquals(0,call(HttpMethod.POST,"/api/app/todos/"+editedId+"/delete",Map.of("idempotencyKey","cross-user-key-12345"),otherToken).path("code").asInt());
            data(call(HttpMethod.POST,"/api/app/todos/"+editedId+"/delete",Map.of("idempotencyKey","todo-delete-12345"),token));
            assertEquals(0,data(call(HttpMethod.GET,"/api/app/todos?status=PENDING",null,token)).path("pendingCount").asInt());
            data(call(HttpMethod.POST,"/api/app/todos/"+id+"/delete",Map.of("idempotencyKey","todo-delete-67890"),token));
            assertEquals(0,data(call(HttpMethod.GET,"/api/app/todos?status=COMPLETED",null,token)).path("completedCount").asInt());
        } finally {
            jdbc.update("DELETE a FROM ha_todo_delivery_attempt a JOIN ha_todo_occurrence o ON o.id=a.occurrence_id WHERE o.user_id=?",userId);
            jdbc.update("DELETE d FROM ha_todo_delivery d JOIN ha_todo_occurrence o ON o.id=d.occurrence_id WHERE o.user_id=?",userId);
            jdbc.update("DELETE FROM ha_todo_request WHERE user_id=?",userId);
            jdbc.update("DELETE FROM ha_todo_occurrence WHERE user_id=?",userId);
            jdbc.update("DELETE FROM ha_todo_rule WHERE user_id=?",userId);
            jdbc.update("DELETE FROM app_login_log WHERE user_id IN (?,?)",userId,otherId);
            jdbc.update("DELETE FROM app_user WHERE id IN (?,?)",userId,otherId);
        }
    }
    private String login(String key,String account,String password) throws Exception {
        JsonNode captcha=data(call(HttpMethod.GET,"/api/app/auth/captcha",null,null));
        String svg=new String(Base64.getDecoder().decode(captcha.path("image").asText().split(",")[1]),StandardCharsets.UTF_8);
        String answer=svg.substring(svg.lastIndexOf("fill='#278879'>")+"fill='#278879'>".length(),svg.indexOf("</text>"));
        return data(call(HttpMethod.POST,"/api/app/auth/h5/login",Map.of("account",account,"encryptedPassword",encrypt(key,password),"captchaId",captcha.path("captchaId").asText(),"captchaCode",answer),null)).path("token").asText();
    }
    private JsonNode call(HttpMethod method,String path,Object payload,String token) throws Exception {
        HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_JSON);
        if(token!=null)headers.set("X-Auth-Token",token);
        return json.readTree(http.exchange(path,method,new HttpEntity<>(payload,headers),String.class).getBody());
    }
    private JsonNode data(JsonNode result) {assertEquals(0,result.path("code").asInt(-1),result.path("message").asText());return result.path("data");}
    private String encrypt(String key,String plain) throws Exception {
        var publicKey=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key)));
        Cipher cipher=Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.ENCRYPT_MODE,publicKey,new OAEPParameterSpec("SHA-256","MGF1",MGF1ParameterSpec.SHA256,PSource.PSpecified.DEFAULT));
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }
}
