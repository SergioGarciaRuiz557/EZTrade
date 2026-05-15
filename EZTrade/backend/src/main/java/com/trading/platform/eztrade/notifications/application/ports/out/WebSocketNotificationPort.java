package com.trading.platform.eztrade.notifications.application.ports.out;

import com.trading.platform.eztrade.notifications.domain.NotificationMessage;

/**
 * Puerto de salida para entregar notificaciones en tiempo real por WebSocket.
 * <p>
 * El adaptador actual usa STOMP/SimpMessagingTemplate, pero el servicio de
 * aplicacion solo necesita este contrato.
 */
public interface WebSocketNotificationPort {

    /** Envia el mensaje al destino WebSocket del usuario destinatario. */
    void send(NotificationMessage message);
}

