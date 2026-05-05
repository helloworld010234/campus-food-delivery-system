package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.service.TokenBlacklistService;
import com.sky.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        BaseContext.clear();
    }

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

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

    @Test
    void preHandle_shouldPopulateBaseContext_fromValidClaims() {
        String token = JwtUtil.createJWT(SECRET, TTL, Map.of(
                JwtClaimsConstant.EMP_ID, 42L,
                JwtClaimsConstant.MERCHANT_ID, 7L,
                JwtClaimsConstant.ACCOUNT_TYPE, 2
        ));
        when(jwtProperties.getAdminTokenName()).thenReturn("token");
        when(jwtProperties.getAdminSecretKey()).thenReturn(SECRET);
        when(request.getHeader("token")).thenReturn(token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
        assertThat(BaseContext.getCurrentId()).isEqualTo(42L);
        assertThat(BaseContext.getCurrentMerchantId()).isEqualTo(7L);
        assertThat(BaseContext.getCurrentAccountType()).isEqualTo(2);
    }

    @Test
    void afterCompletion_shouldClearBaseContext() {
        BaseContext.setCurrentId(99L);
        BaseContext.setCurrentMerchantId(5L);
        BaseContext.setCurrentAccountType(1);

        interceptor.afterCompletion(request, response, handlerMethod, null);

        assertThat(BaseContext.getCurrentId()).isNull();
        assertThat(BaseContext.getCurrentMerchantId()).isNull();
        assertThat(BaseContext.getCurrentAccountType()).isNull();
    }

    @Test
    void preHandle_shouldPopulateOnlyEmpId_whenMerchantAndAccountTypeAbsent() {
        // Legacy admin token without merchant/accountType claims must still
        // pass the interceptor without leaking stale ThreadLocal state. This
        // mirrors compatibility mode where only EMP_ID is present.
        String token = JwtUtil.createJWT(SECRET, TTL, Map.of(
                JwtClaimsConstant.EMP_ID, 11L
        ));
        when(jwtProperties.getAdminTokenName()).thenReturn("token");
        when(jwtProperties.getAdminSecretKey()).thenReturn(SECRET);
        when(request.getHeader("token")).thenReturn(token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
        assertThat(BaseContext.getCurrentId()).isEqualTo(11L);
        assertThat(BaseContext.getCurrentMerchantId()).isNull();
        assertThat(BaseContext.getCurrentAccountType()).isNull();
    }

    @Test
    void preHandle_shouldReturnFalse_whenTokenHeaderMissing() {
        // Missing token header must still fall through to the parse step,
        // which fails -> 401. Critically, blacklist must not be consulted
        // for a null token.
        when(jwtProperties.getAdminTokenName()).thenReturn("token");
        when(request.getHeader("token")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isFalse();
        verify(response).setStatus(401);
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
    }
}
