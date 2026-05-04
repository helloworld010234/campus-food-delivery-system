package com.sky.task;

import com.sky.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class TokenBlacklistCleanupTask {

    private final TokenBlacklistService tokenBlacklistService;

    public TokenBlacklistCleanupTask(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Scheduled(cron = "${sky.jwt.blacklist-cleanup-cron:0 0 3 * * ?}")
    public void run() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        int deleted = tokenBlacklistService.cleanupExpired(threshold);
        log.info("Token blacklist cleanup completed, deleted {} records older than {}", deleted, threshold);
    }
}
