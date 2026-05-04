package com.sky.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ServerEndpoint(value = "/ws/{sid}", configurator = WebSocketAuthConfigurator.class)
public class WebSocketServer {

    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid, EndpointConfig config) {
        Long empId = (Long) config.getUserProperties().get("empId");
        if (empId == null || !String.valueOf(empId).equals(sid)) {
            log.warn("WebSocket connection rejected: empId={} does not match sid={}", empId, sid);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            } catch (Exception e) {
                log.error("Failed to close unauthorized WebSocket session, sid={}", sid, e);
            }
            return;
        }
        SESSION_MAP.put(sid, session);
        log.info("WebSocket connection established, sid={}", sid);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        // no-op
    }

    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        SESSION_MAP.remove(sid);
        log.info("WebSocket connection closed, sid={}", sid);
    }

    public void sendToAllClient(String message) {
        Collection<Session> sessions = SESSION_MAP.values();
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("Failed to broadcast WebSocket message", e);
            }
        }
    }

    public void sendToClient(String sid, String message) {
        Session session = SESSION_MAP.get(sid);
        if (session == null) {
            log.warn("No WebSocket session found for sid={}", sid);
            return;
        }
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to sid={}", sid, e);
        }
    }
}
