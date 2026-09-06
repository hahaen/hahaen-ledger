package com.hahaen.ledger.common.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.List;

/** 将 Sa-Token 会话持久化到 Redis，并隔离在项目统一的 haji 命名空间。 */
@Configuration
public class SaTokenRedisConfig {
    private static final String KEY_PREFIX = "haji:";

    @Bean
    public SaTokenDao saTokenDao(RedisConnectionFactory connectionFactory) {
        return new PrefixedSaTokenDao(connectionFactory);
    }

    private static final class PrefixedSaTokenDao extends SaTokenDaoForRedisTemplate {
        private PrefixedSaTokenDao(RedisConnectionFactory connectionFactory) {
            init(connectionFactory);
        }

        @Override
        public String get(String key) {
            return super.get(prefixed(key));
        }

        @Override
        public void set(String key, String value, long timeout) {
            super.set(prefixed(key), value, timeout);
        }

        @Override
        public void update(String key, String value) {
            super.update(prefixed(key), value);
        }

        @Override
        public void delete(String key) {
            super.delete(prefixed(key));
        }

        @Override
        public long getTimeout(String key) {
            return super.getTimeout(prefixed(key));
        }

        @Override
        public void updateTimeout(String key, long timeout) {
            super.updateTimeout(prefixed(key), timeout);
        }

        @Override
        public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
            return super.searchData(prefixed(prefix), keyword, start, size, sortType).stream()
                    .map(this::withoutPrefix)
                    .toList();
        }

        private static String prefixed(String key) {
            return key.startsWith(KEY_PREFIX) ? key : KEY_PREFIX + key;
        }

        private String withoutPrefix(String key) {
            return key.startsWith(KEY_PREFIX) ? key.substring(KEY_PREFIX.length()) : key;
        }
    }
}
