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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WebSocketAuthConfigurator extends ServerEndpointConfig.Configurator {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
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
            log.warn("WebSocket handshake rejected: invalid token");
            throw new BaseException("Unauthorized");
        }
    }
}
