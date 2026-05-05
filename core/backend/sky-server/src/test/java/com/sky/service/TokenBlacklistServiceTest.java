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

    @Test
    void addToBlacklist_shouldHashDifferentTokens_toDifferentValues() {
        // Distinct tokens must never collide, otherwise one logout could
        // invalidate another active session.
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now().plusHours(2);
        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);

        tokenBlacklistService.addToBlacklist("token.alpha", "ADMIN", "LOGOUT", expiresAt);
        tokenBlacklistService.addToBlacklist("token.beta", "ADMIN", "LOGOUT", expiresAt);

        verify(tokenBlacklistMapper, times(2)).insert(captor.capture());
        String firstHash = captor.getAllValues().get(0).getTokenHash();
        String secondHash = captor.getAllValues().get(1).getTokenHash();
        assertThat(firstHash).isNotEqualTo(secondHash);
        assertThat(firstHash).hasSize(64); // SHA-256 hex
        assertThat(secondHash).hasSize(64);
    }

    @Test
    void addToBlacklist_shouldHashSameTokenDeterministically() {
        // Same token revoked twice must hit the same hash so the existing
        // unique constraint on token_hash works as expected.
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now().plusHours(2);
        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);

        tokenBlacklistService.addToBlacklist("token.same", "ADMIN", "LOGOUT", expiresAt);
        tokenBlacklistService.addToBlacklist("token.same", "ADMIN", "LOGOUT", expiresAt);

        verify(tokenBlacklistMapper, times(2)).insert(captor.capture());
        assertThat(captor.getAllValues().get(0).getTokenHash())
                .isEqualTo(captor.getAllValues().get(1).getTokenHash());
    }

    @Test
    void isBlacklisted_shouldNotPropagateException_evenForNullToken() {
        // SHA-256 of a null token would NPE; the service must remain
        // resilient: a query failure must not block legitimate traffic.
        boolean result = tokenBlacklistService.isBlacklisted(null);

        assertThat(result).isFalse();
    }
}
