package com.hahaen.ledger.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    @Test
    void unauthenticatedBusinessErrorUses401ForApiClients() {
        var response = new GlobalExceptionHandler().handleBusiness(new BusinessException("AUTH_REQUIRED", "请先登录"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(1, response.getBody().code());
    }
}
