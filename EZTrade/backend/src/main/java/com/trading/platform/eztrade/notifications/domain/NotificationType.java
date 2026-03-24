package com.trading.platform.eztrade.notifications.domain;

/**
 * Tipos funcionales de notificacion para clasificacion y trazabilidad.
 * <p>
 * Se usa para etiquetar el mensaje en todos los canales y facilitar filtrado,
 * metricas, auditoria y futuras preferencias de usuario por tipo.
 */
public enum NotificationType {
    /** Notificacion emitida cuando una orden se registra. */
    ORDER_PLACED,
    /** Notificacion emitida cuando una orden se ejecuta. */
    ORDER_EXECUTED,
    /** Notificacion emitida cuando una orden se cancela. */
    ORDER_CANCELLED,
    /** Notificacion emitida cuando portfolio recalcula su valoracion agregada. */
    PORTFOLIO_VALUATION_UPDATED
}

