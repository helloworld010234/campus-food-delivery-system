package com.sky.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WebSocketTask {

    /**
     * Legacy demo broadcast disabled.
     * Admin consoles now expect structured order-event JSON over WebSocket.
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void sendMessageToClient() {
        // no-op
    }
}
