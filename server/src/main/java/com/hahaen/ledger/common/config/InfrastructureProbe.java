package com.hahaen.ledger.common.config;

import com.hahaen.ledger.file.service.MinioStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class InfrastructureProbe {
    private static final Logger log = LoggerFactory.getLogger(InfrastructureProbe.class);
    private final StringRedisTemplate redis;
    private final MinioStorageService storage;

    public InfrastructureProbe(StringRedisTemplate redis, MinioStorageService storage) {
        this.redis = redis;
        this.storage = storage;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verify() throws Exception {
        if (Boolean.TRUE.equals(redis.getConnectionFactory().getConnection().ping() != null)) {
            log.info("DEV/运行时 Redis PING succeeded");
        }
        boolean exists = storage.bucketExists();
        if (!exists) storage.ensureBucket();
        log.info("Configured MinIO bucket {} is available", storage.bucketName());
    }
}
