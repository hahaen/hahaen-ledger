package com.hahaen.ledger.auth.service;

import com.hahaen.ledger.user.entity.AppLoginLog;
import com.hahaen.ledger.user.mapper.AppLoginLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginAuditService {
    private final AppLoginLogMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(AppLoginLog log) {
        mapper.insert(log);
    }
}
