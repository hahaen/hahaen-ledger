package com.hahaen.ledger.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hahaen.ledger.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class WechatApiClient implements WechatCode2SessionClient {
    private static final Logger log = LoggerFactory.getLogger(WechatApiClient.class);
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;
    private final String appId;
    private final String appSecret;

    public WechatApiClient(ObjectMapper objectMapper,
                           @Value("${hahaen.wechat.app-id:}") String appId,
                           @Value("${hahaen.wechat.app-secret:}") String appSecret) {
        this.objectMapper = objectMapper;
        this.appId = appId;
        this.appSecret = appSecret;
    }

    @Override
    public WechatCode2Session exchange(String code) {
        if (!hasText(appId) || !hasText(appSecret)) {
            log.error("微信 code2Session 配置缺失，appid 或 appsecret 未配置");
            throw new BusinessException("WECHAT_CONFIG_INVALID", "微信登录配置不完整");
        }
        try {
            String responseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.weixin.qq.com")
                            .path("/sns/jscode2session")
                            .queryParam("appid", appId)
                            .queryParam("secret", appSecret)
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    // 微信接口在部分环境返回 text/plain，即使内容是 JSON。
                    .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                    .retrieve()
                    .body(String.class);
            if (!hasText(responseBody)) {
                throw new BusinessException("WECHAT_API_UNAVAILABLE", "微信登录服务暂时不可用");
            }
            WechatResponse response;
            try {
                response = objectMapper.readValue(responseBody, WechatResponse.class);
            } catch (JsonProcessingException ex) {
                log.error("微信 code2Session 返回内容无法解析，异常类型={}", ex.getClass().getName());
                throw new BusinessException("WECHAT_API_UNAVAILABLE", "微信登录服务暂时不可用");
            }
            return new WechatCode2Session(response.openId(), response.sessionKey(), response.unionId(),
                    response.errorCode(), response.errorMessage());
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientException ex) {
            // 不记录异常消息：RestClient 异常消息可能包含带 secret 查询参数的完整 URL。
            log.error("微信 code2Session 调用失败，异常类型={}", ex.getClass().getName());
            throw new BusinessException("WECHAT_API_UNAVAILABLE", "微信登录服务暂时不可用");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record WechatResponse(
            @JsonProperty("openid") String openId,
            @JsonProperty("session_key") String sessionKey,
            @JsonProperty("unionid") String unionId,
            @JsonProperty("errcode") Integer errorCode,
            @JsonProperty("errmsg") String errorMessage
    ) {
    }
}
