package com.hahaen.ledger.auth.service;

import com.hahaen.ledger.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WechatApiClient {
    private final RestClient client = RestClient.create();
    @Value("${hahaen.wechat.app-id}") private String appId;
    @Value("${hahaen.wechat.app-secret}") private String appSecret;
    public Session exchangeCode(String code) {
        WechatSession response = client.get().uri(uri -> uri.scheme("https").host("api.weixin.qq.com").path("/sns/jscode2session").queryParam("appid", appId).queryParam("secret", appSecret).queryParam("js_code", code).queryParam("grant_type", "authorization_code").build()).retrieve().body(WechatSession.class);
        if (response == null || response.errcode() != null && response.errcode() != 0 || response.openid() == null || response.openid().isBlank()) throw new BusinessException("WECHAT_LOGIN_FAILED", "微信登录失败，请稍后重试");
        return new Session(response.openid(), response.unionid());
    }
    public record Session(String openId, String unionId) {}
    private record WechatSession(String openid, String unionid, Integer errcode, String errmsg) {}
}
