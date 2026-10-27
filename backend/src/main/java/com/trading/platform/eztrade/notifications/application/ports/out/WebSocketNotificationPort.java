package com.trading.platform.eztrade.notifications.application.ports.out;

import com.trading.platform.eztrade.notifications.domain.NotificationMessage;

/**
 * Output port for delivering real-time notifications through WebSocket.
 * <p>
 * The current adapter uses STOMP/SimpMessagingTemplate, but the application
 * service only needs this contract.
 */
public interface WebSocketNotificationPort {

    /** Sends the message to the recipient user's WebSocket destination. */
    void send(NotificationMessage message);
}

