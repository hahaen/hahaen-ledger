package com.hahaen.ledger.auth.service;

public interface WechatCode2SessionClient {
    WechatCode2Session exchange(String code);

    record WechatCode2Session(String openId, String sessionKey, String unionId,
                              Integer errorCode, String errorMessage) {
    }
}
