package com.hahaen.ledger.todo.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

/** 固定官方 HTTPS 端点；Key 只在请求体中出现，异常消息和响应体不进入日志。 */
@Component
public class TodoNotificationSender {
    private final RestClient client;
    public TodoNotificationSender() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(8000);
        client = RestClient.builder().requestFactory(factory).build();
    }
    public boolean send(String channel, String key, String title, String body) {
        JsonNode response;
        if ("BARK".equals(channel)) {
            response = client.post().uri("https://api.day.app/push")
                    .body(Map.of("device_key",key,"title",title,"body",body,"group","待办清单"))
                    .retrieve().body(JsonNode.class);
        } else if ("PUSHPLUS".equals(channel)) {
            response = client.post().uri("https://www.pushplus.plus/send")
                    .body(Map.of("token",key,"title",title,"content",body,"template","txt"))
                    .retrieve().body(JsonNode.class);
        } else return false;
        return response != null && response.path("code").asInt(-1) == 200;
    }
}
