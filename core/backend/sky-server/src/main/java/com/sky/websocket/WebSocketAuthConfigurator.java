package com.sky.websocket;

import com.sky.constant.JwtClaimsConstant;
import com.sky.exception.BaseException;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WebSocketAuthConfigurator extends ServerEndpointConfig.Configurator implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static JwtProperties staticJwtProperties;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
        try {
            staticJwtProperties = context.getBean(JwtProperties.class);
        } catch (Exception ignored) {
            staticJwtProperties = null;
        }
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        JwtProperties jwtProperties = staticJwtProperties;
        if (jwtProperties == null && applicationContext != null) {
            try {
                jwtProperties = applicationContext.getBean(JwtProperties.class);
                staticJwtProperties = jwtProperties;
            } catch (Exception ignored) {
                jwtProperties = null;
            }
        }
        if (jwtProperties == null) {
            log.error("JwtProperties not initialized — Spring bean injection failed");
            throw new BaseException("Unauthorized");
        }

        Map<String, List<String>> parameterMap = request.getParameterMap();
        List<String> tokens = parameterMap.get("token");

        if (tokens == null || tokens.isEmpty()) {
            log.warn("WebSocket handshake rejected: missing token");
            throw new BaseException("Unauthorized");
        }

        String token = tokens.get(0);

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            config.getUserProperties().put("empId", empId);
            log.debug("WebSocket handshake accepted for empId={}", empId);
        } catch (Exception e) {
            log.warn("WebSocket handshake rejected: invalid token", e);
            throw new BaseException("Unauthorized");
        }
    }
}
