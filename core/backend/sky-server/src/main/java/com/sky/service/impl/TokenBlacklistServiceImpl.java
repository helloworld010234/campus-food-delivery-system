package com.sky.service.impl;

import com.sky.entity.TokenBlacklist;
import com.sky.mapper.TokenBlacklistMapper;
import com.sky.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final TokenBlacklistMapper tokenBlacklistMapper;

    public TokenBlacklistServiceImpl(TokenBlacklistMapper tokenBlacklistMapper) {
        this.tokenBlacklistMapper = tokenBlacklistMapper;
    }

    @Override
    public void addToBlacklist(String token, String tokenType, String reason, LocalDateTime expiresAt) {
        String hash = sha256Hex(token);
        TokenBlacklist record = TokenBlacklist.builder()
                .tokenHash(hash)
                .tokenType(tokenType)
                .reason(reason)
                .expiresAt(expiresAt)
                .build();
        tokenBlacklistMapper.insert(record);
        log.debug("Token added to blacklist, hash={}", hash);
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            String hash = sha256Hex(token);
            Optional<TokenBlacklist> result = tokenBlacklistMapper.selectByTokenHash(hash);
            return result.isPresent();
        } catch (Exception e) {
            log.error("Blacklist query failed, allowing request", e);
            return false;
        }
    }

    @Override
    public int cleanupExpired(LocalDateTime before) {
        return tokenBlacklistMapper.deleteByExpiresAtBefore(before);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 computation failed", e);
        }
    }
}
