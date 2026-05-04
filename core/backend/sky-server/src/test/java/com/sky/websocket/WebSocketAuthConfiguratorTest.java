package com.sky.websocket;

import com.sky.constant.JwtClaimsConstant;
import com.sky.exception.BaseException;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthConfiguratorTest {

    @Mock
    private HandshakeRequest request;

    @Mock
    private HandshakeResponse response;

    @Mock
    private ServerEndpointConfig config;

    @Mock
    private JwtProperties jwtProperties;

    private WebSocketAuthConfigurator configurator;

    private static final String SECRET_KEY = "testsecretkeytestsecretkeytestsecretkey";
    private static final long TTL = 7200000L;

    @BeforeEach
    void setUp() {
        configurator = new WebSocketAuthConfigurator();
        configurator.setJwtProperties(jwtProperties);
    }

    @AfterEach
    void tearDown() {
        configurator.setJwtProperties(null);
    }

    @Test
    void shouldThrowBaseException_whenTokenIsMissing() {
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        assertThatThrownBy(() -> configurator.modifyHandshake(config, request, response))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void shouldThrowBaseException_whenTokenIsForged() {
        Map<String, List<String>> params = new HashMap<>();
        params.put("token", Collections.singletonList("fake.jwt.token"));
        when(request.getParameterMap()).thenReturn(params);
        when(jwtProperties.getAdminSecretKey()).thenReturn(SECRET_KEY);

        assertThatThrownBy(() -> configurator.modifyHandshake(config, request, response))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void shouldPutEmpIdIntoUserProperties_whenTokenIsValid() {
        when(jwtProperties.getAdminSecretKey()).thenReturn(SECRET_KEY);

        String token = JwtUtil.createJWT(SECRET_KEY, TTL,
                java.util.Map.of(JwtClaimsConstant.EMP_ID, 5L));

        Map<String, List<String>> params = new HashMap<>();
        params.put("token", Collections.singletonList(token));
        when(request.getParameterMap()).thenReturn(params);

        Map<String, Object> userProperties = new HashMap<>();
        when(config.getUserProperties()).thenReturn(userProperties);

        configurator.modifyHandshake(config, request, response);

        assertThat(userProperties).containsEntry("empId", 5L);
    }
}
