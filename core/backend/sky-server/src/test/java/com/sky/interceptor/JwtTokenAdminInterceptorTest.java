package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.properties.JwtProperties;
import com.sky.service.TokenBlacklistService;
import com.sky.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenAdminInterceptorTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtTokenAdminInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    private static final String SECRET = "testsecretkeytestsecretkeytestsecretkey";
    private static final long TTL = 7200000L;

    @Test
    void preHandle_shouldReturnFalse_whenTokenIsBlacklisted() {
        String token = "blacklisted.token";
        when(jwtProperties.getAdminTokenName()).thenReturn("token");
        when(request.getHeader("token")).thenReturn(token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isFalse();
        verify(response).setStatus(401);
    }

    @Test
    void preHandle_shouldProceed_whenTokenNotBlacklisted() {
        String token = JwtUtil.createJWT(SECRET, TTL, Map.of(
                JwtClaimsConstant.EMP_ID, 1L,
                JwtClaimsConstant.MERCHANT_ID, 2L,
                JwtClaimsConstant.ACCOUNT_TYPE, 0
        ));
        when(jwtProperties.getAdminTokenName()).thenReturn("token");
        when(jwtProperties.getAdminSecretKey()).thenReturn(SECRET);
        when(request.getHeader("token")).thenReturn(token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void preHandle_shouldReturnTrue_whenNotHandlerMethod() {
        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    void preHandle_shouldReturnFalse_whenTokenInvalid() {
        when(jwtProperties.getAdminTokenName()).thenReturn("token");
        when(request.getHeader("token")).thenReturn("invalid.token");
        when(tokenBlacklistService.isBlacklisted("invalid.token")).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isFalse();
        verify(response).setStatus(401);
    }
}
