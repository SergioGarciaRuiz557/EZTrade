package com.trading.platform.eztrade.notifications.application.ports.out;

import com.trading.platform.eztrade.notifications.domain.NotificationMessage;

/**
 * Puerto de salida para enviar notificaciones push.
 * <p>
 * Permite que {@code NotificationService} publique en un canal movil sin
 * depender de SDKs o proveedores externos.
 */
public interface PushNotificationPort {

    /** Envia el mensaje ya construido al canal push. */
    void send(NotificationMessage message);
}

