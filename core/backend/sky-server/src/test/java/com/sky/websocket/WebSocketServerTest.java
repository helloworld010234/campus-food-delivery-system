package com.sky.websocket;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketServerTest {

    private WebSocketServer webSocketServer;

    @Mock
    private Session session;

    @Mock
    private EndpointConfig config;

    @Mock
    private jakarta.websocket.RemoteEndpoint.Basic remote;

    @BeforeEach
    void setUp() {
        webSocketServer = new WebSocketServer();
    }

    @Test
    void shouldCloseSession_whenEmpIdDoesNotMatchSid() throws Exception {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("empId", 5L);
        when(config.getUserProperties()).thenReturn(userProperties);

        webSocketServer.onOpen(session, "3", config);

        ArgumentCaptor<CloseReason> captor = ArgumentCaptor.forClass(CloseReason.class);
        verify(session).close(captor.capture());
        assertThat(captor.getValue().getCloseCode()).isEqualTo(CloseReason.CloseCodes.VIOLATED_POLICY);
    }

    @Test
    void shouldCloseSession_whenEmpIdIsMissing() throws Exception {
        when(config.getUserProperties()).thenReturn(new HashMap<>());

        webSocketServer.onOpen(session, "5", config);

        ArgumentCaptor<CloseReason> captor = ArgumentCaptor.forClass(CloseReason.class);
        verify(session).close(captor.capture());
        assertThat(captor.getValue().getCloseCode()).isEqualTo(CloseReason.CloseCodes.VIOLATED_POLICY);
    }

    @Test
    void shouldAddToSessionMap_whenEmpIdMatchesSid() throws Exception {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("empId", 5L);
        when(config.getUserProperties()).thenReturn(userProperties);
        when(session.getBasicRemote()).thenReturn(remote);

        webSocketServer.onOpen(session, "5", config);

        verify(session, never()).close(any(CloseReason.class));
        webSocketServer.sendToClient("5", "test message");
        verify(remote).sendText("test message");
    }

    @Test
    void shouldRemoveFromSessionMap_onClose() throws Exception {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("empId", 5L);
        when(config.getUserProperties()).thenReturn(userProperties);

        webSocketServer.onOpen(session, "5", config);
        webSocketServer.onClose("5");

        webSocketServer.sendToClient("5", "test message");
        verify(remote, never()).sendText(anyString());
    }

    @Test
    void shouldDoNothing_whenSendToClientWithNullSession() {
        webSocketServer.sendToClient("nonexistent", "test message");
        verify(session, never()).getBasicRemote();
    }

    @Test
    void shouldHandleSendToClientException() throws Exception {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("empId", 5L);
        when(config.getUserProperties()).thenReturn(userProperties);
        when(session.getBasicRemote()).thenReturn(remote);

        webSocketServer.onOpen(session, "5", config);

        doThrow(new RuntimeException("send failed")).when(remote).sendText(anyString());

        webSocketServer.sendToClient("5", "test message");
        verify(remote).sendText("test message");
    }

    @Test
    void shouldHandleSendToAllClientException() throws Exception {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("empId", 5L);
        when(config.getUserProperties()).thenReturn(userProperties);
        when(session.getBasicRemote()).thenReturn(remote);

        webSocketServer.onOpen(session, "5", config);

        doThrow(new RuntimeException("send failed")).when(remote).sendText(anyString());

        webSocketServer.sendToAllClient("broadcast message");
        verify(remote).sendText("broadcast message");
    }
}
