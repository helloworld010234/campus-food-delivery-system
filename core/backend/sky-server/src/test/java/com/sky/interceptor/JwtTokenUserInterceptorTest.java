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
class JwtTokenUserInterceptorTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtTokenUserInterceptor interceptor;

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
        when(jwtProperties.getUserTokenName()).thenReturn("token");
        when(request.getHeader("token")).thenReturn(token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isFalse();
        verify(response).setStatus(401);
    }

    @Test
    void preHandle_shouldProceed_whenTokenNotBlacklisted() {
        String token = JwtUtil.createJWT(SECRET, TTL, Map.of(
                JwtClaimsConstant.USER_ID, 1L
        ));
        when(jwtProperties.getUserTokenName()).thenReturn("token");
        when(jwtProperties.getUserSecretKey()).thenReturn(SECRET);
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
        when(jwtProperties.getUserTokenName()).thenReturn("token");
        when(request.getHeader("token")).thenReturn("invalid.token");
        when(tokenBlacklistService.isBlacklisted("invalid.token")).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isFalse();
        verify(response).setStatus(401);
    }

    @Test
    void preHandle_shouldPopulateUserId_fromValidClaims() {
        String token = JwtUtil.createJWT(SECRET, TTL, Map.of(
                JwtClaimsConstant.USER_ID, 77L
        ));
        when(jwtProperties.getUserTokenName()).thenReturn("token");
        when(jwtProperties.getUserSecretKey()).thenReturn(SECRET);
        when(request.getHeader("token")).thenReturn(token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
        assertThat(BaseContext.getCurrentId()).isEqualTo(77L);
    }

    @Test
    void afterCompletion_shouldClearBaseContext() {
        BaseContext.setCurrentId(77L);

        interceptor.afterCompletion(request, response, handlerMethod, null);

        assertThat(BaseContext.getCurrentId()).isNull();
    }

    @Test
    void preHandle_shouldReturnFalse_whenTokenHeaderMissing() {
        // Private user endpoints must reject missing tokens with 401 and
        // must not consult the blacklist for a null token.
        when(jwtProperties.getUserTokenName()).thenReturn("token");
        when(request.getHeader("token")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isFalse();
        verify(response).setStatus(401);
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
    }

    @Test
    void preHandle_shouldNotPopulateMerchantId_forUserToken() {
        // User-side JWT does not carry merchantId - guard helpers must rely
        // on the explicit merchant id from the request payload, not on the
        // ThreadLocal context. This pins that boundary.
        String token = JwtUtil.createJWT(SECRET, TTL, Map.of(
                JwtClaimsConstant.USER_ID, 99L
        ));
        when(jwtProperties.getUserTokenName()).thenReturn("token");
        when(jwtProperties.getUserSecretKey()).thenReturn(SECRET);
        when(request.getHeader("token")).thenReturn(token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
        assertThat(BaseContext.getCurrentMerchantId()).isNull();
        assertThat(BaseContext.getCurrentAccountType()).isNull();
    }
}
