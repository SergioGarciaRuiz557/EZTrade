package com.trading.platform.eztrade.notifications.application.ports.out;

import com.trading.platform.eztrade.notifications.domain.NotificationMessage;

/**
 * Puerto de salida para persistir notificaciones en la bandeja interna.
 * <p>
 * Actua como historial auditable y como respaldo cuando el usuario no esta
 * conectado al canal WebSocket.
 */
public interface InboxNotificationPort {

    /** Guarda el mensaje como notificacion no leida en la bandeja del usuario. */
    void save(NotificationMessage message);
}

