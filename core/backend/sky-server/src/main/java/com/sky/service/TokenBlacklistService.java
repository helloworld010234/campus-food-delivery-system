package com.sky.service;

import java.time.LocalDateTime;

public interface TokenBlacklistService {
    void addToBlacklist(String token, String tokenType, String reason);
    boolean isBlacklisted(String token);
    int cleanupExpired(LocalDateTime before);
}
