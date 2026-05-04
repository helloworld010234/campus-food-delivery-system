package com.sky.service;

import java.time.LocalDateTime;

public interface TokenBlacklistService {
    void addToBlacklist(String token, String tokenType, String reason, java.time.LocalDateTime expiresAt);
    boolean isBlacklisted(String token);
    int cleanupExpired(LocalDateTime before);
}
