package io.codebuddy.closetbuddy.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 테스트 환경에서 Redis 기반 CacheManager 대신 NoOpCacheManager를 사용하여
 * Redis 연결 없이도 @Cacheable/@CacheEvict 등이 정상 동작하도록 처리
 */
@TestConfiguration
public class TestCacheConfig {

    @Bean(name = "testCacheManager")
    @Primary
    public CacheManager testCacheManager() {
        return new NoOpCacheManager();
    }
}
