package com.sky.service;

import com.sky.entity.TokenBlacklist;
import com.sky.mapper.TokenBlacklistMapper;
import com.sky.service.impl.TokenBlacklistServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private TokenBlacklistMapper tokenBlacklistMapper;

    @InjectMocks
    private TokenBlacklistServiceImpl tokenBlacklistService;

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
            throw new RuntimeException(e);
        }
    }

    @Test
    void isBlacklisted_shouldReturnTrue_whenTokenInBlacklist() {
        String token = "test.jwt.token";
        String hash = sha256Hex(token);
        when(tokenBlacklistMapper.selectByTokenHash(hash)).thenReturn(Optional.of(new TokenBlacklist()));

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isTrue();
    }

    @Test
    void isBlacklisted_shouldReturnFalse_whenTokenNotInBlacklist() {
        String token = "not.blacklisted";
        String hash = sha256Hex(token);
        when(tokenBlacklistMapper.selectByTokenHash(hash)).thenReturn(Optional.empty());

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isFalse();
    }

    @Test
    void isBlacklisted_shouldReturnFalse_whenMapperThrowsException() {
        String token = "any.token";
        when(tokenBlacklistMapper.selectByTokenHash(anyString())).thenThrow(new RuntimeException("DB down"));

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isFalse();
    }

    @Test
    void addToBlacklist_shouldStoreTokenHash() {
        String token = "test.token";
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now().plusHours(2);

        tokenBlacklistService.addToBlacklist(token, "ADMIN", "LOGOUT", expiresAt);

        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);
        verify(tokenBlacklistMapper).insert(captor.capture());
        TokenBlacklist captured = captor.getValue();
        assertThat(captured.getTokenHash()).isNotBlank();
        assertThat(captured.getTokenType()).isEqualTo("ADMIN");
        assertThat(captured.getReason()).isEqualTo("LOGOUT");
        assertThat(captured.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void cleanupExpired_shouldReturnDeletedCount() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        when(tokenBlacklistMapper.deleteByExpiresAtBefore(threshold)).thenReturn(5);

        int result = tokenBlacklistService.cleanupExpired(threshold);

        assertThat(result).isEqualTo(5);
    }
}
