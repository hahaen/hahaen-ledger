package com.hahaen.ledger.auth.dto;
import jakarta.validation.constraints.NotBlank;
public record WechatLoginRequest(@NotBlank String code) {}
