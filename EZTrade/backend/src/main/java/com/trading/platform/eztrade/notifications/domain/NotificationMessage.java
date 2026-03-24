package com.trading.platform.eztrade.notifications.domain;

import java.time.LocalDateTime;

/**
 * Mensaje de notificacion normalizado para todos los canales.
 * <p>
 * Sirve como DTO interno del modulo para que email/push/websocket/inbox
 * consuman la misma estructura, independientemente del evento origen.
 *
 * @param recipient identificador del destinatario (usuario/email)
 * @param type tipo funcional de la notificacion
 * @param title titulo breve orientado a UI/canal
 * @param body contenido descriptivo del evento
 * @param occurredAt instante en el que ocurrio el evento origen
 */
public record NotificationMessage(
        String recipient,
        NotificationType type,
        String title,
        String body,
        LocalDateTime occurredAt
) {
}

