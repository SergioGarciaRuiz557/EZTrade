package com.trading.platform.eztrade.notifications.application.ports.out;

import com.trading.platform.eztrade.notifications.domain.NotificationMessage;

/**
 * Puerto de salida para enviar notificaciones por email.
 * <p>
 * La aplicacion solo conoce este contrato; la implementacion concreta puede ser
 * un proveedor real de correo o, como en desarrollo, un adaptador de logging.
 */
public interface EmailNotificationPort {

    /** Envia el mensaje ya formateado al canal de email. */
    void send(NotificationMessage message);
}

