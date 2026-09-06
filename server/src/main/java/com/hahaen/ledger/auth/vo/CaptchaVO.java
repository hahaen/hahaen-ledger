package com.hahaen.ledger.auth.vo;

public record CaptchaVO(String captchaId, String image, int expiresInSeconds) {
}
