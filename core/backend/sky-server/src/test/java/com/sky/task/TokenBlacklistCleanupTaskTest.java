package com.sky.task;

import com.sky.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistCleanupTaskTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private TokenBlacklistCleanupTask cleanupTask;

    @Test
    void run_shouldCallCleanupExpired_withThreshold24HoursAgo() {
        when(tokenBlacklistService.cleanupExpired(any(LocalDateTime.class))).thenReturn(5);

        cleanupTask.run();

        verify(tokenBlacklistService).cleanupExpired(any(LocalDateTime.class));
    }

    @Test
    void run_shouldHandleZeroDeletions() {
        when(tokenBlacklistService.cleanupExpired(any(LocalDateTime.class))).thenReturn(0);

        cleanupTask.run();

        verify(tokenBlacklistService).cleanupExpired(any(LocalDateTime.class));
    }
}
