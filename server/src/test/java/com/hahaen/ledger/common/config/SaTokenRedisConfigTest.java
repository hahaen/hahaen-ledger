package com.hahaen.ledger.common.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SaTokenRedisConfigTest {
    @Test
    void createsRedisBackedSaTokenDao() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);

        SaTokenDao dao = new SaTokenRedisConfig().saTokenDao(connectionFactory);

        assertThat(dao).isInstanceOf(SaTokenDaoForRedisTemplate.class);
    }
}
