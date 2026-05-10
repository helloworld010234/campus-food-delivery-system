package com.sky.health;

import com.sky.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class OssHealthIndicator implements HealthIndicator {

    private final AliOssUtil aliOssUtil;

    private volatile Health cachedHealth = Health.down().withDetail("reason", "initializing").build();
    private volatile Instant lastCheck = Instant.MIN;
    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    @Override
    public Health health() {
        Instant now = Instant.now();
        if (Duration.between(lastCheck, now).compareTo(CACHE_TTL) > 0) {
            synchronized (this) {
                if (Duration.between(lastCheck, now).compareTo(CACHE_TTL) > 0) {
                    cachedHealth = performCheck();
                    lastCheck = now;
                }
            }
        }
        return cachedHealth;
    }

    private Health performCheck() {
        try {
            boolean connected = aliOssUtil.validateConnection();
            if (connected) {
                return Health.up().withDetail("service", "Aliyun OSS").build();
            }
            return Health.down().withDetail("service", "Aliyun OSS").withDetail("reason", "bucket not accessible").build();
        } catch (Exception ex) {
            log.warn("OSS health check failed", ex);
            return Health.down().withDetail("service", "Aliyun OSS").withDetail("reason", ex.getMessage()).build();
        }
    }
}
